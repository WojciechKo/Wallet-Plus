package info.korzeniowski.walletplus.datamanager.local;

import com.j256.ormlite.dao.Dao;

import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.AccountDataManager;
import info.korzeniowski.walletplus.model.Account;

public class LocalAccountDataManager implements AccountDataManager{
    private final Dao<Account, Long> accountDao;

    @Inject
    public LocalAccountDataManager(Dao<Account, Long> accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Long insert(Account entity) {
        return null;
    }

    @Override
    public Long count() {
        return null;
    }

    @Override
    public Account findById(Long id) {
        return null;
    }

    @Override
    public List<Account> getAll() {
        return null;
    }

    @Override
    public void update(Account entity) {

    }

    @Override
    public void deleteById(Long id) {

    }
}
