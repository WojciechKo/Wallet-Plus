package info.korzeniowski.walletplus.datamanager.local;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.local.modelfactory.CategoryFactory;
import info.korzeniowski.walletplus.datamanager.local.validation.CategoryValidator;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.GreenCategory;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocalCategoryDataManager implements CategoryDataManager {
    private final GreenCategoryDao greenCategoryDao;
    private final CategoryValidator categoryValidator;

    private final List<Category> categories;
    private final List<Category> mainCategories;

    @Inject
    public LocalCategoryDataManager(GreenCategoryDao greenCategoryDao) {
        this.greenCategoryDao = greenCategoryDao;
        categories = getCategoryListFromGreenCategoryList(greenCategoryDao.loadAll());
        mainCategories = filterMainCategoriesFromCategories(categories);
        Collections.sort(mainCategories, Category.Comparators.NAME);
        this.categoryValidator = new CategoryValidator(this);
    }

    private List<Category> filterMainCategoriesFromCategories(List<Category> categories) {
        return Lists.newArrayList(Iterables.filter(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category category) {
                return category.getParentId() == null;
            }
        }));
    }
    /*************
     * CREATE
     *************/
    @Override
    public Long insert(Category category) {
        checkNotNull(category);
        categoryValidator.validateInsert(category);

        category.setId(greenCategoryDao.insert(CategoryFactory.createGreenCategory(category)));
        if (category.getParentId() == null) {
            insertMain(category);
        } else {
            insertSub(category);
        }
        categories.add(category);
        Collections.sort(categories, Category.Comparators.NAME);
        return category.getId();
    }

    private void insertMain(Category category) {
        mainCategories.add(category);
        Collections.sort(mainCategories, Category.Comparators.POSITION);
    }

    private void insertSub(Category category) {
        Category parent;
        parent = findInMainCategoriesById(category.getParentId());
        category.setTypes(parent.getTypes());
        category.setParent(parent);
        parent.getChildren().add(category);
        Collections.sort(parent.getChildren(), Category.Comparators.NAME);
    }

    /*************
     * READ
     *************/
    @Override
    public Category findById(final Long id) {
        Category found = Category.findById(categories, id);
        return CategoryFactory.createCategory(found);
    }

    @Override
    public Category findByName(final String name) {
        Category found = Category.findByName(categories, name);
        return CategoryFactory.createCategory(found);
    }

    @Override
    public List<Category> getAll() {
        return CategoryFactory.copyOfCategoriesWithParentAndChildren(categories);
    }

    @Override
    public Long count() {
        return greenCategoryDao.count();
    }

    @Override
    public List<Category> getMainCategories() {
        return CategoryFactory.copyOfMainCategoriesWithChildren(mainCategories);
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
        result = CategoryFactory.copyOfMainCategoriesWithChildren(result);
        Collections.sort(result, Category.Comparators.POSITION);
        return result;
    }

    @Override
    public List<Category> getSubCategoriesOf(Long id) {
        Category category = findInMainCategoriesById(id);
        return CategoryFactory.copyOfMainCategoryWithChildren(category).getChildren();
    }

    /*************
     * UPDATE
     *************/
    @Override
    public void update(final Category newValue) {
        Category toUpdate = Category.findById(categories, newValue.getId());
        categoryValidator.validateUpdate(newValue, toUpdate);
        greenCategoryDao.update(CategoryFactory.createGreenCategory(newValue));
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
                for (Category category : toUpdate.getChildren()) {
                    category.setTypes(updated.getTypes());
                }
            }

            @Override
            protected void mainToSubApply(Category updated, Category toUpdate) {
                Category parent = Category.findById(categories, updated.getParentId());
                mainCategories.remove(toUpdate);
                parent.addChild(toUpdate);
                toUpdate.setParent(parent);
                toUpdate.setParentId(parent.getId());
            }

            @Override
            protected void subToMainApply(Category updated, Category toUpdate) {
                Category parent = Category.findById(categories, toUpdate.getParentId());
                parent.getChildren().remove(toUpdate);
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
        categoryValidator.validateDelete(categoryToDelete);
        if (categoryToDelete.getParentId() == null) {
            deleteMainCategory(categoryToDelete);
        } else {
            deleteSubCategory(categoryToDelete);
        }
    }

    private void deleteMainCategory(final Category categoryToDelete) {
        greenCategoryDao.deleteByKey(categoryToDelete.getId());
        removeById(mainCategories, categoryToDelete.getId());
        removeById(categories, categoryToDelete.getId());
    }

    private void deleteSubCategory(final Category categoryToDelete) {
        Category parentCategory = findInMainCategoriesById(categoryToDelete.getParentId());
        greenCategoryDao.deleteByKey(categoryToDelete.getId());
        parentCategory.removeChild(categoryToDelete);
        removeById(categories, categoryToDelete.getId());
    }

    @Override
    public void deleteByIdWithSubcategories(Long id) {
        Category category = findInMainCategoriesById(id);
        List<Category> children = category.getChildren();

        greenCategoryDao.queryBuilder().where(GreenCategoryDao.Properties.ParentId.eq(id)).buildDelete().executeDeleteWithoutDetachingEntities();
        categories.removeAll(children);
        category.getChildren().clear();

        deleteById(id);
    }

    private List<Category> getCategoryListFromGreenCategoryList(List<GreenCategory> greenCategoryList) {
        return CategoryFactory.createCategoryList(greenCategoryList);
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
            if(newValue.getParentId() == null && toUpdate.getParentId() == null) {
                mainToMainApply(newValue, toUpdate);
            } else if (toUpdate.getParentId() == null) {
                mainToSubApply(newValue, toUpdate);
            } else if(newValue.getParentId() == null) {
                subToMainApply(newValue, toUpdate);
            } else {
                subToSubApply(newValue, toUpdate);
            }
            commonApply(newValue, toUpdate);
        }

        protected abstract void commonApply(Category newValue, Category toUpdate);
        protected abstract void mainToMainApply(Category newValue, Category toUpdate);
        protected abstract void mainToSubApply(Category newValue, Category toUpdate);
        protected abstract void subToMainApply(Category newValue, Category toUpdate);
        protected abstract void subToSubApply(Category newValue, Category toUpdate);
    }
}
