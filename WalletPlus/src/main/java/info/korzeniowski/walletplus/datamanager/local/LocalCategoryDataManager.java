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
import info.korzeniowski.walletplus.model.greendao.CategoryG;
import info.korzeniowski.walletplus.model.greendao.CategoryGDao;

public class LocalCategoryDataManager implements CategoryDataManager {
    private CategoryGDao categoryDao;
    private List<Category> categories;
    private List<Category> mainCategories;

    @Inject
    public LocalCategoryDataManager(CategoryGDao categoryDao) {
        this.categoryDao = categoryDao;
        categories = getAll();
        mainCategories = getMainCategories();
    }

    @Override
    public List<Category> getAll() {
        return getCategoryListFromCategoryGList(categoryDao.loadAll());
    }

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

    @Override
    public Long count() {
        return categoryDao.count();
    }

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

    private void ifSubGetTypesFromParent(Category category) {
        if (category.getParentId() != null) {
            Category parent = findInMainCategoriesById(category.getParentId());
            category.setTypes(parent.getTypes());
        }
    }

    @Override
    public Category getByName(final String name) {
        return Iterables.find(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category input) {
                return Objects.equal(input.getName(), name);
            }
        });
    }

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
        categoryDao.update(new CategoryG(category));
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

    @Override
    public Long insert(Category category) {
        Preconditions.checkNotNull(category);
        validateInsert(category);

        category.setId(categoryDao.insert(new CategoryG(category)));
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

    @Override
    public void deleteById(Long id) {
        Category categoryToDelete = getById(id);
        if (categoryToDelete.getParent() == null) {
            deleteMainCategory(categoryToDelete);
        } else {
            deleteSubCategory(categoryToDelete);
        }
    }

    private void deleteMainCategory(final Category categoryToDelete) {
        if (categoryToDelete.getChildren().isEmpty()) {
            categoryDao.deleteByKey(categoryToDelete.getId());
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
        categoryDao.deleteByKey(categoryToDelete.getId());
        parentCategory.getChildren().remove(categoryToDelete);
    }

    @Override
    public void deleteByIdWithSubcategories(Long id) {
        categoryDao.queryBuilder().where(CategoryGDao.Properties.ParentId.eq(id)).buildDelete().executeDeleteWithoutDetachingEntities();
        Category category = findInMainCategoriesById(id);
        category.getChildren().clear();

        deleteById(id);
    }

    private List<Category> getCategoryListFromCategoryGList(List<CategoryG> categoryGList) {
        List<Category> categoryList = new ArrayList<Category>();
        for(CategoryG categoryG : categoryGList) {
            categoryList.add(CategoryG.toCategory(categoryG));
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
