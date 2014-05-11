package info.korzeniowski.walletplus.datamanager.local;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.CannotDeleteCategoryWithChildrenException;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.CategoryWithGivenNameAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.ParentIsNotMainCategoryException;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.GreenCategory;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;

public class LocalCategoryDataManager implements CategoryDataManager {
    private GreenCategoryDao greenCategoryDao;
    private List<Category> categories;
    private List<Category> mainCategories;

    @Inject
    public LocalCategoryDataManager(GreenCategoryDao greenCategoryDao) {
        this.greenCategoryDao = greenCategoryDao;
        categories = getAll();
        mainCategories = getMainCategories();
    }

    /*************
     * CREATE
     *************/

    @Override
    public Long insert(Category category) {
        Preconditions.checkNotNull(category);
        validateInsert(category);

        category.setId(greenCategoryDao.insert(new GreenCategory(category)));
        categories.add(category);
        if (category.getParentId() == null) {
            insertMain(category);
        } else {
            insertSub(category);
        }
        Collections.sort(categories, Category.Comparators.NAME);
        Collections.sort(mainCategories, Category.Comparators.POSITION);
        return category.getId();
    }

    private void insertMain(Category category) {
        mainCategories.add(category);
    }

    private void insertSub(Category category) {
        category.setTypes(EnumSet.noneOf(Category.Type.class));
        Category parent;
        try {
            parent = findInMainCategoriesById(category.getParentId());
        } catch (NoSuchElementException e) {
            throw new ParentIsNotMainCategoryException(category.getId(), category.getParentId());
        }
        parent.getChildren().add(category);
        Collections.sort(parent.getChildren(), Category.Comparators.POSITION);
    }

    private void validateInsert(Category category) {
        validateUniqueId(category);
        validateUniqueName(category);
    }

    /*************
     * READ
     *************/

    @Override
    public Category getById(final Long id) {
        Preconditions.checkNotNull(id);

        Category result = Iterables.find(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getId(), id);
            }
        });
        ifSubGetTypesFromParent(result);
        return result;
    }

    private Category getByName(final String name) {
        return Iterables.find(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getName(), name);
            }
        });
    }

    @Override
    public List<Category> getAll() {
        return getCategoryListFromGreenCategoryList(greenCategoryDao.loadAll());
    }

    @Override
    public Long count() {
        return (long) categories.size();
    }

    @Override
    public List<Category> getMainCategories() {
        if (mainCategories == null) {
            mainCategories = Lists.newArrayList();
            for (Category category : categories) {
                if (category.getParentId() == null) {
                    mainCategories.add(category);
                }
            }
            Collections.sort(mainCategories, Category.Comparators.NAME);
        }
        return mainCategories;
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
        Collections.sort(result, Category.Comparators.POSITION);
        return result;
    }

    private void ifSubGetTypesFromParent(Category category) {
        if (category.getParentId() != null) {
            Category parent = findInMainCategoriesById(category.getParentId());
            category.setTypes(parent.getTypes());
        }
    }

    /*************
     * UPDATE
     *************/

    @Override
    public void update(final Category category) {
        validateUpdate(category);
        Category toUpdate = getById(category.getId());
        if (toUpdate.getParentId() == null) {
            updateMain(category, toUpdate);
        } else {
            updateSub(category);
        }
        toUpdate.setTypes(category.getTypes());
        toUpdate.setName(category.getName());
        greenCategoryDao.update(new GreenCategory(category));
    }

    private void updateMain(final Category newCategory, final Category oldCategory) {
        // Main => Sub
        if (newCategory.getParentId() != null) {
            if (!oldCategory.getChildren().isEmpty()) {
                throw new ParentIsNotMainCategoryException(oldCategory.getChildren().get(0).getId(), oldCategory.getId());
            } else {
                Category parent = findInMainCategoriesById(newCategory.getParentId());
                Iterables.removeIf(mainCategories, new Predicate<Category>() {
                    @Override
                    public boolean apply(Category input) {
                        return Objects.equal(input.getId(), newCategory.getId());
                    }
                });
                parent.addChild(oldCategory);
            }
        }

    }

    private void updateSub(Category category) {

    }


    private void validateUpdate(Category category) {
        validateUniqueName(category);
    }

    private void validateUniqueId(Category category) {
        if (category.getId() == null) return;

        try {
            getById(category.getId());
        } catch (NoSuchElementException e) {
            return;
        }
        throw new EntityAlreadyExistsException(category, category.getId());
    }

    private void validateUniqueName(Category category) {
        try {
            getByName(category.getName());
        } catch (NoSuchElementException e) {
            return;
        }
        throw new CategoryWithGivenNameAlreadyExistsException(category.getName());
    }

    /*************
     * DELETE
     *************/

    @Override
    public void deleteById(Long id) {
        Category categoryToDelete = getById(id);
        if (categoryToDelete.getParent() == null) {
            deleteMainCategory(categoryToDelete);
        } else {
            deleteSubCategory(categoryToDelete);
        }
    }

    @Override
    public void deleteByIdWithSubcategories(Long id) {
        greenCategoryDao.queryBuilder().where(GreenCategoryDao.Properties.ParentId.eq(id)).buildDelete().executeDeleteWithoutDetachingEntities();
        Category category = findInMainCategoriesById(id);
        category.getChildren().clear();
        deleteById(id);
    }

    private void deleteMainCategory(final Category categoryToDelete) {
        if (categoryToDelete.getChildren().isEmpty()) {
            greenCategoryDao.deleteByKey(categoryToDelete.getId());
            Iterables.removeIf(mainCategories, new Predicate<Category>() {
                @Override
                public boolean apply(Category input) {
                    return Objects.equal(input.getId(), categoryToDelete.getId());
                }
            });
        } else {
            throw new CannotDeleteCategoryWithChildrenException(categoryToDelete);
        }
    }

    private void deleteSubCategory(Category categoryToDelete) {
        Category parentCategory = findInMainCategoriesById(categoryToDelete.getParentId());
        greenCategoryDao.deleteByKey(categoryToDelete.getId());
        parentCategory.getChildren().remove(categoryToDelete);
    }

    private List<Category> getCategoryListFromGreenCategoryList(List<GreenCategory> greenCategoryList) {
        List<Category> categoryList = new ArrayList<Category>();
        for(GreenCategory greenCategory : greenCategoryList) {
            categoryList.add(GreenCategory.toCategory(greenCategory));
        }
        return categoryList;
    }

    private Category findInMainCategoriesById(final Long id) {
        return Iterables.find(mainCategories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getId(), id);
            }
        });
    }
}
