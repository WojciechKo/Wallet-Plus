package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;

import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.service.AccountService;
import info.korzeniowski.walletplus.model.Account;

public class LocalAccountService implements AccountService {
    private final Dao<Account, Long> accountDao;

    @Inject
    public LocalAccountService(Dao<Account, Long> accountDao) {
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
