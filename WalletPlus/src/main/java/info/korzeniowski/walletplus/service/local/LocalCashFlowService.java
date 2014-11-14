package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;
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
    private Category other;
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
    public long countAssignedToWallet(Long walletId) {
        try {
            return cashFlowDao.queryBuilder().where().eq("fromWallet_id", walletId).or().eq("toWallet_id", walletId).countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Category getOtherCategory() {
        try {
            if (other == null) {
                other = categoryDao.queryBuilder().where().eq("type", Category.Type.OTHER).queryForFirst();
            }
            return other;
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
            boolean isFirst = true;
            Where<CashFlow, Long> where = cashFlowDao.queryBuilder().where();
            if (from != null) {
//                if (!isFirst) {
//                    where.and();
//                }
                isFirst = false;
                where.ge("dateTime", from);
            }
            if (to != null) {
                if (!isFirst) {
                    where.and();
                }
                isFirst = false;
                where.lt("dateTime", to);
            }
            if (categoryId != null) {
                if (!isFirst) {
                    where.and();
                }
                isFirst = false;
                where.eq("category_id", categoryId);
            }
            if (fromWalletId != null) {
                if (!isFirst) {
                    where.and();
                }
                isFirst = false;
                where.eq("fromWallet_id", fromWalletId);
            }
            if (toWalletId != null) {
                if (!isFirst) {
                    where.and();
                }
//                isFirst = false;
                where.eq("toWallet_id", toWalletId);
            }
            return where.query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
