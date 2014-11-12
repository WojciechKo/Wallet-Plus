package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.validation.WalletValidator;

public class LocalWalletService implements WalletService {
    private WalletValidator walletValidator;
    private final Dao<Wallet, Long> walletDao;
    private Dao<CashFlow, Long> cashFlowDao;

    @Inject
    public LocalWalletService(Dao<Wallet, Long> walletDao, Dao<CashFlow, Long> cashFlowDao) {
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
            return walletDao.queryBuilder().orderByRaw("name COLLATE NOCASE").query();
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
            DeleteBuilder db = cashFlowDao.deleteBuilder();
            db.where()
                .eq("fromWallet_id", id)
                .or()
                    .eq("toWallet_id", id);
            cashFlowDao.delete(db.prepare());
            walletDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Wallet> getMyWallets() {
        try {
            return walletDao.queryBuilder().orderByRaw("name COLLATE NOCASE").where().eq("type", Wallet.Type.MY_WALLET).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Wallet> getContractors() {
        try {
            return walletDao.queryBuilder().orderByRaw("name COLLATE NOCASE").where().eq("type", Wallet.Type.CONTRACTOR).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public WalletValidator getWalletValidator() {
        return walletValidator;
    }

    public void setWalletValidator(WalletValidator walletValidator) {
        this.walletValidator = walletValidator;
    }
}
