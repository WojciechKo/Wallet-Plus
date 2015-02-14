package info.korzeniowski.walletplus.service.local;

import com.google.common.base.Preconditions;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.CategoryAndCashFlowBind;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;

public class LocalCashFlowService implements CashFlowService {
    private final Dao<CashFlow, Long> cashFlowDao;
    private final Dao<Wallet, Long> walletDao;
    private final Dao<Category, Long> categoryDao;
    private final Dao<CategoryAndCashFlowBind, Long> categoryAndCashFlowBindsDao;
    private final LocalCategoryService localCategoryService;

    private PreparedQuery<Category> categoriesForCashFlowQuery;

    @Inject
    public LocalCashFlowService(Dao<CashFlow, Long> cashFlowDao,
                                Dao<Wallet, Long> walletDao,
                                Dao<Category, Long> categoryDao,
                                Dao<CategoryAndCashFlowBind, Long> categoryAndCashFlowBindsDao,
                                LocalCategoryService localCategoryService) {
        this.cashFlowDao = cashFlowDao;
        this.walletDao = walletDao;
        this.categoryDao = categoryDao;
        this.categoryAndCashFlowBindsDao = categoryAndCashFlowBindsDao;
        this.localCategoryService = localCategoryService;
    }

    @Override
    public Long count() {
        try {
            return cashFlowDao.countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public CashFlow findById(final Long id) {
        try {
            CashFlow cashFlow = cashFlowDao.queryForId(id);
            cashFlow.addCategory(getCategoriesForCashFlow(cashFlow.getId()));
            return cashFlow;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<CashFlow> getAll() {
        try {
            List<CashFlow> cashFlows = cashFlowDao.queryBuilder().orderBy("dateTime", false).query();
            for (CashFlow cashFlow : cashFlows) {
                cashFlow.addCategory(getCategoriesForCashFlow(cashFlow.getId()));
            }

            return cashFlows;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private List<Category> getCategoriesForCashFlow(Long cashFlowId) throws SQLException {
        if (categoriesForCashFlowQuery == null) {
            categoriesForCashFlowQuery = getCategoriesForCashFlowQuery();
        }
        categoriesForCashFlowQuery.setArgumentHolderValue(0, cashFlowId);
        return categoryDao.query(categoriesForCashFlowQuery);
    }

    private PreparedQuery<Category> getCategoriesForCashFlowQuery() throws SQLException {
        QueryBuilder<CategoryAndCashFlowBind, Long> categoryCashFlowQb = categoryAndCashFlowBindsDao.queryBuilder();
        categoryCashFlowQb.selectColumns(CategoryAndCashFlowBind.CATEGORY_ID_FIELD_NAME);
        SelectArg cashFlowIdArg = new SelectArg();
        categoryCashFlowQb.where().eq(CategoryAndCashFlowBind.CASH_FLOW_ID_FIELD_NAME, cashFlowIdArg);

        QueryBuilder<Category, Long> categoryQb = categoryDao.queryBuilder();
        categoryQb.where().in(Category.ID_FIELD_NAME, categoryCashFlowQb);
        return categoryQb.prepare();
    }

    @Override
    public void update(CashFlow cashFlow) {
        try {
            CashFlow toUpdate = findById(cashFlow.getId());
            validateUpdate(toUpdate, cashFlow);
            cashFlowDao.update(cashFlow);
            unbindWithCategories(toUpdate);
            bindWithCategories(cashFlow);
            fixCurrentAmountInWalletAfterDelete(toUpdate);
            fixCurrentAmountInWalletAfterInsert(cashFlow);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void validateUpdate(CashFlow old, CashFlow newValue) {

    }

    @Override
    public Long insert(CashFlow cashFlow) {
        try {
            cashFlowDao.create(cashFlow);
            bindWithCategories(cashFlow);
            fixCurrentAmountInWalletAfterInsert(cashFlow);
            return cashFlow.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void bindWithCategories(CashFlow cashFlow) throws SQLException {
        for (Category category : cashFlow.getCategories()) {
            Category foundCategory = localCategoryService.findByName(category.getName());

            if (foundCategory == null) {
                foundCategory = category;
                localCategoryService.insert(foundCategory);
            } else {
                category.setId(foundCategory.getId());
            }

            categoryAndCashFlowBindsDao.create(new CategoryAndCashFlowBind(foundCategory, cashFlow));
        }
    }

    private void fixCurrentAmountInWalletAfterInsert(CashFlow cashFlow) throws SQLException {
        Wallet wallet = cashFlow.getWallet();

        Double newCurrentAmount = null;
        if (CashFlow.Type.INCOME.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() + cashFlow.getAmount();
        } else if (CashFlow.Type.EXPANSE.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() - cashFlow.getAmount();
        }
        Preconditions.checkNotNull(newCurrentAmount);
        wallet.setCurrentAmount(newCurrentAmount);
        walletDao.update(wallet);
    }

    @Override
    public void deleteById(Long id) {
        try {
            CashFlow cashFlow = cashFlowDao.queryForId(id);
            cashFlowDao.deleteById(id);
            unbindWithCategories(cashFlow);
            fixCurrentAmountInWalletAfterDelete(cashFlow);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void unbindWithCategories(CashFlow cashFlow) throws SQLException {
        DeleteBuilder<CategoryAndCashFlowBind, Long> deleteBuilder = categoryAndCashFlowBindsDao.deleteBuilder();

        deleteBuilder.where()
                .eq(CategoryAndCashFlowBind.CASH_FLOW_ID_FIELD_NAME, cashFlow)
                .and()
                .in(CategoryAndCashFlowBind.CATEGORY_ID_FIELD_NAME, cashFlow.getCategories());

        deleteBuilder.delete();
    }

    private void fixCurrentAmountInWalletAfterDelete(CashFlow cashFlow) throws SQLException {
        Wallet wallet = cashFlow.getWallet();

        Double newCurrentAmount = null;
        if (CashFlow.Type.INCOME.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() - cashFlow.getAmount();
        } else if (CashFlow.Type.EXPANSE.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() + cashFlow.getAmount();
        }
        Preconditions.checkNotNull(newCurrentAmount);
        wallet.setCurrentAmount(newCurrentAmount);
        walletDao.update(wallet);
    }

    @Override
    public long countAssignedWithWallet(Long walletId) {
        try {
            return cashFlowDao.queryBuilder().where().eq("wallet_id", walletId).countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long countAssignedWithCategory(Long categoryId) {
//        try {
            throw new RuntimeException("Not implemented");
//            return cashFlowDao.queryBuilder().where().eq("category_id", categoryId).countOf();
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
    }

    @Override
    public List<CashFlow> findCashFlow(Date from, Date to, Long categoryId, Long walletId) {
        try {
            QueryBuilder<CashFlow, Long> queryBuilder = cashFlowDao.queryBuilder();
            queryBuilder.setWhere(getWhereList(from, to, categoryId, walletId, queryBuilder));
            return queryBuilder.query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private Where<CashFlow, Long> getWhereList(Date from, Date to, Long categoryId, Long walletId, QueryBuilder<CashFlow, Long> queryBuilder) throws SQLException {
        Where<CashFlow, Long> where = queryBuilder.where();
        boolean isFirst = true;

        if (from != null) {
            where.ge("dateTime", from);
            isFirst = false;
        }
        if (to != null) {
            if (!isFirst) {
                where.and();
            }
            where.lt("dateTime", to);
            isFirst = false;
        }
        if (walletId != null) {
            if (!isFirst) {
                where.and();
            }
            if (walletId == WalletService.WALLET_NULL_ID) {
                where.isNull("wallet_id");
            } else {
                where.eq("wallet_id", walletId);
            }
        }

        return where;
    }

    @Override
    public List<CashFlow> getLastNCashFlows(int n) {
        try {
            return cashFlowDao.queryBuilder().orderBy("dateTime", false).limit((long) n).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
