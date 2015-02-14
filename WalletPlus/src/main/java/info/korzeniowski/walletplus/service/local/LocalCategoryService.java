package info.korzeniowski.walletplus.service.local;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.validation.CategoryValidator;
import info.korzeniowski.walletplus.util.KorzeniowskiUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocalCategoryService implements CategoryService {
    private final CategoryValidator categoryValidator;
    private final Dao<Category, Long> categoryDao;

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

    /**
     * *************
     * STATISTICS *
     * *************
     */

    @Override
    public CategoryStats getCategoryStats(Category category, final Date firstDay, final Period period, final Integer iteration) {
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
        List<CashFlow> cashFlowList = cashFlowService.findCashFlow(firstDayArg.toDate(), lastDayArg.toDate(), category.getId(), null);

        CategoryStats stats = new CategoryStats(category.getId());
        for (CashFlow cashFlow : cashFlowList) {
            CashFlow.Type type = cashFlow.getType();
            if (type == CashFlow.Type.INCOME) {
                stats.incomeAmount(cashFlow.getAmount());
            } else if (type == CashFlow.Type.EXPANSE) {
                stats.expanseAmount(cashFlow.getAmount());
            }
        }

        return stats;
    }

    @Override
    public List<CategoryStats> getCategoryStatsList(Date firstDay, Period period, Integer iteration) {
        try {
            List<CategoryStats> result = createCategoryStatsResults(categoryDao.queryForAll());
            List<CashFlow> cashFlowList = getCashFlowList(firstDay, period, iteration);

            for (final CashFlow cashFlow : cashFlowList) {
                List<Category> categories = cashFlow.getCategories();
                for (Category category : categories) {
                    CategoryStats categoryStats = findCategoryStats(result, category);

                    if (cashFlow.getType() == CashFlow.Type.INCOME) {
                        categoryStats.incomeAmount(cashFlow.getAmount());
                    } else if (cashFlow.getType() == CashFlow.Type.EXPANSE) {
                        categoryStats.expanseAmount(cashFlow.getAmount());
                    }
                }
            }

            return result;

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private List<CategoryStats> createCategoryStatsResults(List<Category> categories) {
        List<CategoryStats> result = Lists.newArrayListWithCapacity(categories.size());
        Lists.newArrayListWithCapacity(categories.size());
        for (Category category : categories) {
            result.add(new CategoryStats(category.getId()));
        }
        result.add(new CategoryStats(CATEGORY_NULL_ID));
        return result;
    }

    @Override
    public long countDependentCashFlows(Long categoryId) {
        return cashFlowService.countAssignedWithCategory(categoryId);
    }

    private List<CashFlow> getCashFlowList(Date firstDay, Period period, Integer iteration) {
        checkNotNull(firstDay);
        checkNotNull(period);
        checkNotNull(iteration);

        Interval interval = KorzeniowskiUtils.Times.getInterval(new DateTime(firstDay), period, iteration);
        return cashFlowService.findCashFlow(interval.getStart().toDate(), interval.getEnd().toDate(), null, null);
    }


    private CategoryStats findCategoryStats(List<CategoryStats> list, final Category category) {
        return Iterables.find(list, new Predicate<CategoryStats>() {
            @Override
            public boolean apply(CategoryStats input) {
                if (category == null) {
                    return input.getCategoryId().equals(CATEGORY_NULL_ID);
                }
                return category.getId().equals(input.getCategoryId());
            }
        });
    }
}
