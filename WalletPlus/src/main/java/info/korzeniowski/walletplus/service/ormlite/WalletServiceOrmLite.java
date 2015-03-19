package info.korzeniowski.walletplus.service.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;

import static com.google.common.base.Preconditions.checkNotNull;

public class WalletServiceOrmLite implements WalletService {
    private final Dao<Wallet, Long> walletDao;
    private final Dao<CashFlow, Long> cashFlowDao;

    @Inject
    public WalletServiceOrmLite(Dao<Wallet, Long> walletDao, Dao<CashFlow, Long> cashFlowDao) {
        this.walletDao = walletDao;
        this.cashFlowDao = cashFlowDao;
    }

    @Override
    public Long insert(Wallet wallet) {
        try {
            validateInsert(wallet);
            wallet.setCurrentAmount(wallet.getInitialAmount());
            walletDao.create(wallet);
            return wallet.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void validateInsert(Wallet wallet) {
        checkNotNull(wallet);
        validateIfInitialAmountIsNotNull(wallet);
        validateIfNameIsNotNull(wallet);
    }

    private void validateIfInitialAmountIsNotNull(Wallet wallet) {
        if (wallet.getInitialAmount() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(wallet.getClass().getSimpleName(), Wallet.INITIAL_AMOUNT_COLUMN_NAME);
        }
    }

    private void validateIfNameIsNotNull(Wallet wallet) {
        if (wallet.getName() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(wallet.getClass().getSimpleName(), Wallet.NAME_COLUMN_NAME);
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
            List<Wallet> query = walletDao.queryBuilder().orderByRaw(Wallet.NAME_COLUMN_NAME + " COLLATE NOCASE").query();
            //TODO: Change it to insertion sort.
            Collections.sort(query, new Comparator<Wallet>() {
                @Override
                public int compare(Wallet lhs, Wallet rhs) {
                    return Collator.getInstance().compare(lhs.getName(), rhs.getName());
                }
            });
            return query;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void update(Wallet newValue) {
        try {
            validateUpdate(newValue);
            fixCurrentAmount(newValue);
            walletDao.update(newValue);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void validateUpdate(Wallet newValue) {

    }

    private void fixCurrentAmount(Wallet newValue) {
        Wallet currentValue = findById(newValue.getId());
        newValue.setCurrentAmount(currentValue.getCurrentAmount() + newValue.getInitialAmount() - currentValue.getInitialAmount());
    }

    @Override
    public void deleteById(Long id) {
        try {
            validateDelete(id);
            DeleteBuilder<CashFlow, Long> db = cashFlowDao.deleteBuilder();
            db.where().eq(CashFlow.WALLET_ID_COLUMN_NAME, id);
            cashFlowDao.delete(db.prepare());
            walletDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void validateDelete(Long id) {

    }
}
