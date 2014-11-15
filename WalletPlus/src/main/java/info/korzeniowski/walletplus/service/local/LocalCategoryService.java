package info.korzeniowski.walletplus.service.local;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.KorzeniowskiUtils;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.validation.CategoryValidator;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocalCategoryService implements CategoryService {
    private final CategoryValidator categoryValidator;
    private final Dao<Category, Long> categoryDao;

    @Inject
    @Named("local")
    CashFlowService cashFlowService;

    @Inject
    public LocalCategoryService(Dao<Category, Long> categoryDao) {
        this.categoryDao = categoryDao;
        this.categoryValidator = new CategoryValidator(this);
    }

    public LocalCategoryService(Dao<Category, Long> categoryDao, CategoryValidator categoryValidator) {
        this.categoryDao = categoryDao;
        this.categoryValidator = categoryValidator;
    }

    /**
     * ********
     * CREATE *
     * ********
     */
    @Override
    public Long insert(Category category) {
        try {
            categoryValidator.validateInsert(category);
            categoryDao.create(category);
            return category.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * ******
     * READ *
     * ******
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
            return categoryDao.queryBuilder().orderByRaw("name COLLATE NOCASE").query();
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
            Where where = categoryDao.queryBuilder().orderByRaw("name COLLATE NOCASE").where();
            return where.and(
                    where.isNull("parent_id"),
                    where.or(
                            where.eq("type", Category.Type.INCOME),
                            where.eq("type", Category.Type.INCOME_EXPENSE),
                            where.eq("type", Category.Type.EXPENSE)
                    )
            ).query();
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
            return categoryDao.queryBuilder().orderByRaw("name COLLATE NOCASE").where().eq("type", type).or().eq("type", Category.Type.INCOME_EXPENSE).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Category> getSubCategoriesOf(Long id) {
        try {
            return categoryDao.queryBuilder().orderByRaw("name COLLATE NOCASE").where().eq("parent_id", id).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * ********
     * UPDATE *
     * ********
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
     * ********
     * DELETE *
     * ********
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
            try {
                categoryValidator.validateDelete(id);
            } catch (CategoryHaveSubsException e) {
                // Here we want to delete category with subs
            }
            Category main = findById(id);
            DeleteBuilder builder = categoryDao.deleteBuilder();
            builder.where().eq("parent_id", main.getId());
            builder.delete();
            categoryDao.delete(main);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * *************
     * STATISTICS *
     * *************
     */

    @Override
    public CategoryStats getCategoryStats(Category category, Date firstDay, Period period, Integer iteration) {
        checkNotNull(category);
        checkNotNull(firstDay);
        checkNotNull(period);
        checkNotNull(iteration);

        DateTime firstDayArg;
        if (iteration <= 0) {
            firstDayArg = new DateTime(firstDay).minus(period.multipliedBy(0 - iteration));
        } else {
            firstDayArg = new DateTime(firstDay).plus(period.multipliedBy(iteration));
        }

        DateTime lastDayArg = firstDayArg.plus(period);

        List<CashFlow> cashFlowList = cashFlowService.findCashFlow(firstDayArg.toDate(), lastDayArg.toDate(), category.getId(), null, null);

        Double difference = 0.0;
        Double flow = 0.0;
        for (CashFlow cashFlow : cashFlowList) {
            flow += cashFlow.getAmount();
            CashFlow.Type type = cashFlow.getType();
            if (type == CashFlow.Type.INCOME) {
                difference += cashFlow.getAmount();
            } else if (type == CashFlow.Type.EXPANSE) {
                difference -= cashFlow.getAmount();
            }
        }
        return new CategoryStats(category.getId(), flow, difference);
    }


    @Override
    public List<CategoryStats> getCategoryStateList(Date firstDay, Period period, Integer iteration) {
        List<Category> categories = null;
        try {
            categories = categoryDao.queryForAll();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        List<CategoryStats> result = createCategoryStatsResults(categories);

        List<CashFlow> cashFlowList = getCashFlowList(firstDay, period, iteration);
        for (final CashFlow cashFlow : cashFlowList) {
            CategoryStats found = findByCategory(result, cashFlow.getCategory());
            fixCategoryStats(found, cashFlow);
        }
        return result;
    }

    private List<CategoryStats> createCategoryStatsResults(List<Category> categories) {
        List<CategoryStats> result = Lists.newArrayListWithCapacity(categories.size());
        Lists.newArrayListWithCapacity(categories.size());
        for (Category category : categories) {
            result.add(new CategoryStats(category.getId()));
        }
        return result;
    }

    private List<CashFlow> getCashFlowList(Date firstDay, Period period, Integer iteration) {
        checkNotNull(firstDay);
        checkNotNull(period);
        checkNotNull(iteration);

        Interval interval = KorzeniowskiUtils.Time.getInterval(new DateTime(firstDay), period, iteration);
        return cashFlowService.findCashFlow(interval.getStart().toDate(), interval.getEnd().toDate(), null, null, null);
    }


    private CategoryStats findByCategory(List<CategoryStats> list, final Category category) {
        return Iterables.find(list, new Predicate<CategoryStats>() {
            @Override
            public boolean apply(CategoryStats input) {
                return input.getCategoryId().equals(category.getId());
            }
        });
    }

    private void fixCategoryStats(CategoryStats categoryStats, CashFlow cashFlow) {
        if (cashFlow.getType() == CashFlow.Type.INCOME) {
            categoryStats.incomeAmount(cashFlow.getAmount());
        } else if (cashFlow.getType() == CashFlow.Type.EXPANSE) {
            categoryStats.expanseAmount(cashFlow.getAmount());
        }
    }
}
