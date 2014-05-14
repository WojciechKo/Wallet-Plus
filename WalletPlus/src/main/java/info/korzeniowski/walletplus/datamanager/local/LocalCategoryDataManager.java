package info.korzeniowski.walletplus.datamanager.local;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.exception.ParentCategoryHaveToHaveAtLastOneType;
import info.korzeniowski.walletplus.datamanager.exception.SubCategoryCannotHaveSetTypeException;
import info.korzeniowski.walletplus.datamanager.exception.CannotDeleteCategoryWithChildrenException;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.CategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.datamanager.local.validation.CategoryValidator;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.GreenCategory;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;

public class LocalCategoryDataManager implements CategoryDataManager {
    private GreenCategoryDao greenCategoryDao;
    private List<Category> categories;
    private List<Category> mainCategories;
    private CategoryValidator categoryValidator;

    @Inject
    public LocalCategoryDataManager(GreenCategoryDao greenCategoryDao) {
        this.greenCategoryDao = greenCategoryDao;
        categories = getCategoryListFromGreenCategoryList(greenCategoryDao.loadAll());
        mainCategories = filterMainCategoriesFromCategories(categories);
        Collections.sort(mainCategories, Category.Comparators.NAME);
        categoryValidator = new CategoryValidator(this);
    }

    /*************
     * CREATE
     *************/
    @Override
    public Long insert(Category category) {
        Preconditions.checkNotNull(category);
        categoryValidator.validateInsert(category);

        category.setId(greenCategoryDao.insert(new GreenCategory(category)));
        if (category.getParentId() == null) {
            insertMain(category);
            Collections.sort(mainCategories, Category.Comparators.POSITION);
        } else {
            insertSub(category);
        }
        Collections.sort(categories, Category.Comparators.NAME);
        return category.getId();
    }

    private void insertMain(Category category) {
        if (category.getTypes().size() == 0) {
            throw new ParentCategoryHaveToHaveAtLastOneType();
        }
        categories.add(category);
        mainCategories.add(category);
    }

    private void insertSub(Category category) {
        Category parent;
        try {
            parent = findInMainCategoriesById(category.getParentId());
        } catch (NoSuchElementException e) {
            throw new CategoryIsNotMainCategoryException("Category id: " + category.getParentId());
        }
        parent.getChildren().add(category);
        Collections.sort(parent.getChildren(), Category.Comparators.POSITION);
    }

    /*************
     * READ
     *************/
    @Override
    public Category findById(final Long id) {
        Category found = Iterables.find(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getId(), id);
            }
        });
        getTypeFromParentIfSubCategory(found);
        return new Category(found);
    }

    private void getTypeFromParentIfSubCategory(Category category) {
        if (category.getParentId() != null) {
            Category parent = findInMainCategoriesById(category.getParentId());
            category.setTypes(parent.getTypes());
        }
    }

    @Override
    public Category findByName(final String name) {
        Category found = Iterables.find(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getName(), name);
            }
        });
        return new Category(found);
    }

    @Override
    public List<Category> getAll() {
        return Category.deepCopyOfCategories(categories);
    }

    @Override
    public Long count() {
        return (long) categories.size();
    }

    @Override
    public List<Category> getMainCategories() {
        return Category.deepCopyOfMainCategories(mainCategories);
    }

    private List<Category> filterMainCategoriesFromCategories(List<Category> categories) {
        return Lists.newArrayList(Iterables.filter(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category category) {
                return category.getParentId() == null;
            }
        }));
    }

    @Override
    public List<Category> getMainIncomeTypeCategories() {
        return getMainCategoriesOfType(Category.Type.INCOME);
    }

    @Override
    public List<Category> getMainExpenseTypeCategories() {
        return getMainCategoriesOfType(Category.Type.EXPENSE);
    }

    private List<Category> getMainCategoriesOfType(final Category.Type type) {
        List<Category> result = Lists.newArrayList();
        for (Category category : mainCategories) {
            if (category.getTypes().contains(type)) {
                result.add(category);
            }
        }
        result = Category.deepCopyOfMainCategories(result);
        Collections.sort(result, Category.Comparators.POSITION);
        return result;
    }


    /*************
     * UPDATE
     *************/
    @Override
    public void update(final Category newValue) {
        Category toUpdate = Category.findById(categories, newValue.getId());
        categoryValidator.validateUpdate(newValue, toUpdate);
        greenCategoryDao.update(new GreenCategory(newValue));
        updateCategoryLists(newValue, toUpdate);
    }
    
    private void updateCategoryLists(Category updated, Category toUpdate) {
        new Applier() {
            @Override
            protected void commonApply(Category updated, Category toUpdate) {
                toUpdate.setTypes(updated.getTypes());
                toUpdate.setName(updated.getName());
            }

            @Override
            protected void mainToMainApply(Category updated, Category toUpdate) {

            }

            @Override
            protected void mainToSubApply(Category updated, Category toUpdate) {
                Category parent = Category.findById(categories, updated.getParentId());
                parent.addChild(toUpdate);
                toUpdate.setParent(parent);
                toUpdate.setParentId(parent.getId());
                mainCategories.remove(toUpdate);
            }

            @Override
            protected void subToMainApply(Category updated, Category toUpdate) {
                Category.findById(categories, toUpdate.getParentId()).getChildren().remove(toUpdate);
                mainCategories.add(toUpdate);
            }

            @Override
            protected void subToSubApply(Category updated, Category toUpdate) {
                Category oldParent = Category.findById(categories, toUpdate.getParentId());
                oldParent.getChildren().remove(toUpdate);
                Category newParent = Category.findById(categories, updated.getParentId());
                newParent.getChildren().add(toUpdate);
                toUpdate.setParent(newParent);
            }
        }.apply(updated, toUpdate);
    }

    /*************
     * DELETE
     *************/
    @Override
    public void deleteById(Long id) {
        Category categoryToDelete = findById(id);
        if (categoryToDelete.getParent() == null) {
            deleteMainCategory(categoryToDelete);
        } else {
            deleteSubCategory(categoryToDelete);
        }
    }

    @Override
    public void deleteByIdWithSubcategories(Long id) {
        List<Category> children = getCategoryListFromGreenCategoryList(
                greenCategoryDao.queryBuilder().where(GreenCategoryDao.Properties.ParentId.eq(id)).build().list()
        );
        greenCategoryDao.queryBuilder().where(GreenCategoryDao.Properties.ParentId.eq(id)).buildDelete().executeDeleteWithoutDetachingEntities();
        Category category = findInMainCategoriesById(id);
        category.getChildren().clear();
        categories.removeAll(children);
        deleteById(id);
    }

    private void deleteMainCategory(final Category categoryToDelete) {
        if (categoryToDelete.getChildren().isEmpty()) {
            greenCategoryDao.deleteByKey(categoryToDelete.getId());
            removeById(mainCategories, categoryToDelete.getId());
            removeById(categories, categoryToDelete.getId());
        } else {
            throw new CannotDeleteCategoryWithChildrenException(categoryToDelete);
        }
    }

    private void deleteSubCategory(final Category categoryToDelete) {
        Category parentCategory = findInMainCategoriesById(categoryToDelete.getParentId());
        greenCategoryDao.deleteByKey(categoryToDelete.getId());
        removeById(parentCategory.getChildren(), categoryToDelete.getId());
        removeById(categories, categoryToDelete.getId());
    }

    private List<Category> getCategoryListFromGreenCategoryList(List<GreenCategory> greenCategoryList) {
        return GreenCategory.deepCopyToCategories(greenCategoryList);
    }

    private Category findInMainCategoriesById(final Long id) {
        return Iterables.find(mainCategories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getId(), id);
            }
        });
    }

    private void removeById(List<Category> categories, final Long id) {
        Iterables.removeIf(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getId(), id);
            }
        });
    }

    private abstract class Applier {
        public void apply(Category newValue, Category toUpdate) {
            commonApply(newValue, toUpdate);
            if(newValue.getParentId() == null && toUpdate.getParentId() == null) {
                mainToMainApply(newValue, toUpdate);
            } else if (toUpdate.getParentId() == null) {
                mainToSubApply(newValue, toUpdate);
            } else if(newValue.getParentId() == null) {
                subToMainApply(newValue, toUpdate);
            } else {
                subToSubApply(newValue, toUpdate);
            }
        }

        protected abstract void commonApply(Category newValue, Category toUpdate);
        protected abstract void mainToMainApply(Category newValue, Category toUpdate);
        protected abstract void mainToSubApply(Category newValue, Category toUpdate);
        protected abstract void subToMainApply(Category newValue, Category toUpdate);
        protected abstract void subToSubApply(Category newValue, Category toUpdate);
    }
}
