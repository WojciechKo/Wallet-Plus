package info.korzeniowski.walletplus.datamanager.local;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.dao.query.WhereCondition;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.CategoryHaveChildrenException;
import info.korzeniowski.walletplus.datamanager.NotFoundInMainCategoriesException;
import info.korzeniowski.walletplus.datamanager.ParentCategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.CategoryG;
import info.korzeniowski.walletplus.model.greendao.CategoryGDao;

/**
 * Created by Wojtek on 04.03.14.
 */
public class LocalCategoryDataManager implements CategoryDataManager {

    private CategoryGDao categoryDao;
    private List<Category> categories;

    @Inject
    public LocalCategoryDataManager(CategoryGDao categoryDao) {
        this.categoryDao = categoryDao;
        getMainCategories();
    }

    @Override
    public Long count() {
        return categoryDao.count();
    }

    @Override
    public Category getById(Long id) {
        CategoryG categoryG = categoryDao.loadDeep(id);
        if (categoryG != null) {
            Category category = CategoryG.toCategory(categoryG);
            if (category.getParent() != null) {
                category.setTypes(category.getParent().getTypes());
            }
            return category;
        }
        return null;
    }

    public Category getByName(String name) {
        return CategoryG.toCategory(categoryDao.queryBuilder().where(CategoryGDao.Properties.Name.eq(name)).build().unique());
    }

    @Override
    public List<Category> getAll() {
        return getCategoryListFromCategoryGList(categoryDao.loadAll());
    }

    public List<Category> getMainCategories() {
        List<CategoryG> categoryGList = categoryDao.queryBuilder()
                .where(CategoryGDao.Properties.ParentId.isNull())
                .list();
        categories = getCategoryListFromCategoryGList(categoryGList);
        return categories;
    }

    @Override
    public List<Category> getByMainPosition(int mainPosition) {
        return categories.get(mainPosition).getChildren();
    }

    @Override
    public Category getByMainAndSubPosition(int mainPosition, int subPosition) {
        return categories.get(mainPosition).getChildren().get(subPosition);
    }

    @Override
    public void update(Category category) {
        categoryDao.update(new CategoryG(category));
    }

    @Override
    public Long insert(Category category) {
        checkUnique(category);

        if (category.getParentId() == null) {
            return insertMain(category);
        } else {
            return insertSub(category);
        }
    }

    private void checkUnique(Category category) {
//        if (getById(category.getId()) != null) {
//            throw new EntityAlreadyExistsException(category, category.getId());
//        }
//        else if (getByName(category.getName()) != null) {
//            throw new DuplicateCategoryNameException(category.getName());
//        }
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

    @Override
    public void deleteByIdWithSubcategories(Long id) {
        categoryDao.queryBuilder().where(CategoryGDao.Properties.ParentId.eq(id)).buildDelete().executeDeleteWithoutDetachingEntities();
        Category category = findInMainCategories(id);
        category.getChildren().clear();

        deleteById(id);
        categories.remove(category);
    }

    @Override
    public List<Category> getMainIncomeCategories() {
        return getCategoryListFromCategoryGList(
                categoryDao.queryBuilder().where(
                        new WhereCondition.StringCondition(
                                CategoryGDao.Properties.Type.columnName + " & " + Category.Type.convertEnumToBitwise(Category.Type.INCOME)+ " <> 0")
                ).build().listLazyUncached()
        );
    }

    @Override
    public List<Category> getMainExpenseCategories() {
        return getCategoryListFromCategoryGList(
                categoryDao.queryBuilder().where(
                        new WhereCondition.StringCondition(
                                CategoryGDao.Properties.Type.columnName + " & " + Category.Type.convertEnumToBitwise(Category.Type.EXPENSE)+ " <> 0")
                ).build().listLazyUncached()
        );
    }

    private Category findInMainCategories(Long id) {
        for (Category category : categories) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        throw new NotFoundInMainCategoriesException(id);
    }

    private Long insertMain(Category category) {
        CategoryG categoryG = new CategoryG(category);
        Long id = categoryDao.insert(categoryG);
        categories.add(category.setId(id));
        return id;
    }

    private Long insertSub(Category category) {
        try {
            category.setTypes(EnumSet.noneOf(Category.Type.class));
            findInMainCategories(category.getParentId()).getChildren().add(category);
        } catch (NotFoundInMainCategoriesException e) {
            throw new ParentCategoryIsNotMainCategoryException(category.getId(), category.getParentId());
        }
        return categoryDao.insert(new CategoryG(category));
    }

    private void deleteMainCategory(Category categoryToDelete) {
        if (categoryToDelete.getChildren().isEmpty()) {
            categoryDao.deleteByKey(categoryToDelete.getId());
            categories.remove(categoryToDelete);
        } else {
            throw new CategoryHaveChildrenException();
        }
    }

    private void deleteSubCategory(Category categoryToDelete) {
        Category parentCategory = findInMainCategories(categoryToDelete.getParentId());
        categoryDao.deleteByKey(categoryToDelete.getId());
        parentCategory.getChildren().remove(categoryToDelete);
    }






    private List<Category> getCategoryListFromCategoryGList(List<CategoryG> categoryGList) {
        List<Category> categoryList = new ArrayList<Category>();
        for(CategoryG categoryG : categoryGList) {
            categoryList.add(CategoryG.toCategory(categoryG));
        }
        return categoryList;
    }
}
