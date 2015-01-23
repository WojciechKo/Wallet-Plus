package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;

public class LocalCashFlowService implements CashFlowService {
    private final Dao<CashFlow, Long> cashFlowDao;
    //TODO: zamienić na Service
    private final Dao<Wallet, Long> walletDao;
    //TODO: zamienić na Service
    private final Dao<Category, Long> categoryDao;
    private Category transfer;

    @Inject
    public LocalCashFlowService(Dao<CashFlow, Long> cashFlowDao, Dao<Wallet, Long> walletDao, Dao<Category, Long> categoryDao) {
        this.cashFlowDao = cashFlowDao;
        this.walletDao = walletDao;
        this.categoryDao = categoryDao;
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
            return cashFlowDao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<CashFlow> getAll() {
        try {
            return cashFlowDao.queryBuilder().orderBy("dateTime", false).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void update(CashFlow cashFlow) {
        try {
            CashFlow toUpdate = findById(cashFlow.getId());
            validateUpdate(toUpdate, cashFlow);
            cashFlowDao.update(cashFlow);
            fixCurrentAmountInWalletsAfterUpdate(toUpdate, cashFlow);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void fixCurrentAmountInWalletsAfterUpdate(CashFlow oldCashFlow, CashFlow newCashFlow) throws SQLException {
        fixCurrentAmountInWalletAfterDelete(oldCashFlow);
        newCashFlow.setToWallet(walletDao.queryForId(newCashFlow.getToWallet().getId()));
        newCashFlow.setFromWallet(walletDao.queryForId(newCashFlow.getFromWallet().getId()));
        fixCurrentAmountInWalletsAfterInsert(newCashFlow);
    }

    private void validateUpdate(CashFlow old, CashFlow newValue) {
        //TODO: if exists
    }

    @Override
    public Long insert(CashFlow cashFlow) {
        try {
            validateInsert(cashFlow);
            cashFlowDao.create(cashFlow);
            fixCurrentAmountInWalletsAfterInsert(cashFlow);
            return cashFlow.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void fixCurrentAmountInWalletsAfterInsert(CashFlow cashFlow) throws SQLException {
        Wallet fromWallet = cashFlow.getFromWallet();
        if (fromWallet != null) {
            fromWallet.setCurrentAmount(fromWallet.getCurrentAmount() - cashFlow.getAmount());
            walletDao.update(fromWallet);
        }

        Wallet toWallet = cashFlow.getToWallet();
        if (toWallet != null) {
            toWallet.setCurrentAmount(toWallet.getCurrentAmount() + cashFlow.getAmount());
            walletDao.update(toWallet);
        }
    }

    private void validateInsert(CashFlow cashFlow) {
        if (cashFlow.getType() == CashFlow.Type.TRANSFER) {
            cashFlow.setCompleted(true);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            CashFlow cashFlow = cashFlowDao.queryForId(id);
            cashFlowDao.deleteById(id);
            fixCurrentAmountInWalletAfterDelete(cashFlow);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void fixCurrentAmountInWalletAfterDelete(CashFlow cashFlow) throws SQLException {
        Wallet fromWallet = cashFlow.getFromWallet();
        if (fromWallet != null) {
            fromWallet.setCurrentAmount(fromWallet.getCurrentAmount() + cashFlow.getAmount());
            walletDao.update(fromWallet);
        }

        Wallet toWallet = cashFlow.getToWallet();
        if (toWallet != null) {
            toWallet.setCurrentAmount(toWallet.getCurrentAmount() - cashFlow.getAmount());
            walletDao.update(toWallet);
        }
    }

    @Override
    public long countAssignedWithWallet(Long walletId) {
        try {
            return cashFlowDao.queryBuilder().where().eq("fromWallet_id", walletId).or().eq("toWallet_id", walletId).countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long countAssignedWithCategory(Long categoryId) {
        try {
            return cashFlowDao.queryBuilder().where().eq("category_id", categoryId).countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Category getTransferCategory() {
        try {
            if (transfer == null) {
                transfer = categoryDao.queryBuilder().where().eq("type", Category.Type.TRANSFER).queryForFirst();
            }
            return transfer;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<CashFlow> findCashFlow(Date from, Date to, Long categoryId, Long fromWalletId, Long toWalletId) {
        try {
            QueryBuilder<CashFlow, Long> queryBuilder = cashFlowDao.queryBuilder();
            queryBuilder.setWhere(getWhereList(from, to, categoryId, fromWalletId, toWalletId, queryBuilder));
            return queryBuilder.query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private Where<CashFlow, Long> getWhereList(Date from, Date to, Long categoryId, Long fromWalletId, Long toWalletId, QueryBuilder<CashFlow, Long> queryBuilder) throws SQLException {
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
        if (categoryId != null) {
            if (!isFirst) {
                where.and();
            }
            where.eq("category_id", categoryId);
            isFirst = false;
        }
        if (fromWalletId != null) {
            if (!isFirst) {
                where.and();
            }
            where.eq("fromWallet_id", fromWalletId);
            isFirst = false;
        }
        if (toWalletId != null) {
            if (!isFirst) {
                where.and();
            }
            where.eq("toWallet_id", toWalletId);
        }

        return where;
    }

    @Override
    public List<CashFlow> findCashFlow(Date from, Date to, Category.Type categoryType, Long fromWalletId, Long toWalletId) {
        try {
            QueryBuilder<CashFlow, Long> queryBuilder = cashFlowDao.queryBuilder();

            if (from != null) {
                queryBuilder.where().ge("dateTime", from);
            }
            if (to != null) {
                queryBuilder.where().lt("dateTime", to);
            }
            if (categoryType == Category.Type.NO_CATEGORY) {
                queryBuilder.where().isNull("category_id");
            }
            if (fromWalletId != null) {
                queryBuilder.where().eq("fromWallet_id", fromWalletId);
            }
            if (toWalletId != null) {
                queryBuilder.where().eq("toWallet_id", toWalletId);
            }

            return queryBuilder.query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
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
