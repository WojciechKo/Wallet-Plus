package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.service.AccountService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;

@Singleton
public class LocalAccountService implements AccountService {
    private final Dao<Account, Long> accountDao;

    @Inject
    public LocalAccountService(Dao<Account, Long> accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Long insert(Account entity) {
        try {
            accountDao.create(entity);
            return entity.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Long count() {
        try {
            return accountDao.countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Account findById(Long id) {
        try {
            return accountDao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Account> getAll() {
        try {
            return accountDao.queryBuilder().orderByRaw("name COLLATE NOCASE").query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void update(Account newValue) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void deleteById(Long id) {
        throw new RuntimeException("Not implemented!");
    }

    private void deleteDatabaseFile(String databaseFileName) {
        throw new RuntimeException("Not implemented!");
    }
}
