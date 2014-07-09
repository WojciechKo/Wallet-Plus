package info.korzeniowski.walletplus.datamanager.local;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.DatabaseException;
import info.korzeniowski.walletplus.datamanager.local.validation.CategoryValidator;
import info.korzeniowski.walletplus.model.Category;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocalCategoryDataManager implements CategoryDataManager {
    private final CategoryValidator categoryValidator;
    private final Dao<Category, Long> categoryDao;

    @Inject
    public LocalCategoryDataManager(Dao<Category, Long> categoryDao) {
        this.categoryDao = categoryDao;
        this.categoryValidator = new CategoryValidator(this);
    }

    public LocalCategoryDataManager(Dao<Category, Long> categoryDao, CategoryValidator categoryValidator) {
        this.categoryDao = categoryDao;
        this.categoryValidator = categoryValidator;
    }

    /**
     * **********
     * CREATE
     * ***********
     */
    @Override
    public Long insert(Category category) {
        try {
            checkNotNull(category);
            categoryValidator.validateInsert(category);
            categoryDao.create(category);
            return category.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * **********
     * READ
     * ***********
     */
    @Override
    public Category findById(final Long id) {
        try {
            return categoryDao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Category findByName(final String name) {
        try {
            return categoryDao.queryBuilder().where().eq("name", name).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Category> getAll() {
        try {
            return categoryDao.queryForAll();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Long count() {
        try {
            return categoryDao.countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Category> getMainCategories() {
        try {
            return categoryDao.queryBuilder().where().isNull("parent_id").query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
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
        try {
            return categoryDao.queryBuilder().where().eq("type", type).or().eq("type", Category.Type.INCOME_EXPENSE).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Category> getSubCategoriesOf(Long id) {
        try {
            return categoryDao.queryBuilder().where().eq("parent_id", id).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * **********
     * UPDATE
     * ***********
     */
    @Override
    public void update(final Category newValue) {
        try {
            categoryValidator.validateUpdate(newValue);
            categoryDao.update(newValue);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * **********
     * DELETE
     * ***********
     */
    @Override
    public void deleteById(Long id) {
        try {
            categoryValidator.validateDelete(id);
            categoryDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void deleteByIdWithSubcategories(Long id) {
        try {
            categoryValidator.validateDelete(id);
            Category main = findById(id);
            categoryDao.deleteBuilder().where().eq("parent", main).query();
            categoryDao.delete(main);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
