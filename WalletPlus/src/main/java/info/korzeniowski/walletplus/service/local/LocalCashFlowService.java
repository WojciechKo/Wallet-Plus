package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;

public class LocalCashFlowService implements CashFlowService {
    private final Dao<CashFlow, Long> cashFlowDao;
    private final Dao<Wallet, Long> walletDao;
    private final Dao<Category, Long> categoryDao;

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
            Wallet.Builder builder = new Wallet.Builder(fromWallet);
            builder.setCurrentAmount(fromWallet.getCurrentAmount() - cashFlow.getAmount());
            walletDao.update(builder.build());
        }

        Wallet toWallet = cashFlow.getToWallet();
        if (toWallet != null) {
            Wallet.Builder builder = new Wallet.Builder(toWallet);
            builder.setCurrentAmount(toWallet.getCurrentAmount() + cashFlow.getAmount());
            walletDao.update(builder.build());
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
            Wallet.Builder builder = new Wallet.Builder(fromWallet);
            builder.setCurrentAmount(fromWallet.getCurrentAmount() + cashFlow.getAmount());
            walletDao.update(builder.build());
        }

        Wallet toWallet = cashFlow.getToWallet();
        if (toWallet != null) {
            Wallet.Builder builder = new Wallet.Builder(toWallet);
            builder.setCurrentAmount(toWallet.getCurrentAmount() - cashFlow.getAmount());
            walletDao.update(builder.build());
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
            return categoryDao.queryBuilder().where().eq("type", Category.Type.OTHER).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Category getTransferCategory() {
        try {
            return categoryDao.queryBuilder().where().eq("type", Category.Type.TRANSFER).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }    }
}
