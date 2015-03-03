package info.korzeniowski.walletplus.service.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.ormlite.validation.WalletValidator;

public class WalletServiceOrmLite implements WalletService {
    private final Dao<Wallet, Long> walletDao;
    private final Dao<CashFlow, Long> cashFlowDao;

    @Inject
    CashFlowService cashFlowService;

    private WalletValidator walletValidator;

    @Inject
    public WalletServiceOrmLite(Dao<Wallet, Long> walletDao, Dao<CashFlow, Long> cashFlowDao) {
        this.walletDao = walletDao;
        this.cashFlowDao = cashFlowDao;
        this.walletValidator = new WalletValidator(this);
    }

    @Override
    public Long insert(Wallet wallet) {
        try {
            walletValidator.validateInsert(wallet);
            walletDao.create(wallet);
            return wallet.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Long count() {
        try {
            return walletDao.countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Wallet findById(Long id) {
        try {
            return walletDao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Wallet> getAll() {
        try {
            return walletDao.queryBuilder().orderByRaw(Wallet.NAME_COLUMN_NAME + " COLLATE NOCASE").query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void update(Wallet newValue) {
        try {
            walletValidator.validateUpdate(newValue);
            fixCurrentAmount(newValue);
            walletDao.update(newValue);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void fixCurrentAmount(Wallet newValue) {
        Wallet currentValue = findById(newValue.getId());
        newValue.setCurrentAmount(currentValue.getCurrentAmount() + newValue.getInitialAmount() - currentValue.getInitialAmount());
    }

    @Override
    public void deleteById(Long id) {
        try {
            walletValidator.validateDelete(id);
            DeleteBuilder<CashFlow, Long> db = cashFlowDao.deleteBuilder();
            db.where().eq(CashFlow.WALLET_ID_COLUMN_NAME, id);
            cashFlowDao.delete(db.prepare());
            walletDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long countDependentCashFlows(Long walletId) {
        return cashFlowService.countAssignedWithWallet(walletId);
    }

    public WalletValidator getWalletValidator() {
        return walletValidator;
    }

    public void setWalletValidator(WalletValidator walletValidator) {
        this.walletValidator = walletValidator;
    }
}
