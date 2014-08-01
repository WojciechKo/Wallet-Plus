package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.validation.Validator;
import info.korzeniowski.walletplus.service.local.validation.WalletValidator;
import info.korzeniowski.walletplus.model.Wallet;

public class LocalWalletService implements WalletService {
    Validator<Wallet> walletValidator;
    private final Dao<Wallet, Long> walletDao;

    @Inject
    public LocalWalletService(Dao<Wallet, Long> walletDao) {
        this.walletDao = walletDao;
        this.walletValidator = new WalletValidator(this);
    }

    @Override
    public Long insert(Wallet wallet) {
        try {
            walletValidator.validateInsert(wallet);
            List<Wallet> all = walletDao.queryForAll();
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
            walletDao.update(newValue);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            walletValidator.validateDelete(id);
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

    @Override
    public Wallet findByNameAndType(String name, Wallet.Type type) {
        try {
            return walletDao.queryBuilder().where().eq("name", name).and().eq("type", type).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
