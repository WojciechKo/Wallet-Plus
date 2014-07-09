package info.korzeniowski.walletplus.datamanager.local;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.datamanager.exception.DatabaseException;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;

public class LocalCashFlowDataManager implements CashFlowDataManager {
    private final Dao<CashFlow, Long> cashFlowDao;
    private final Dao<Wallet, Long> walletDao;

    @Inject
    public LocalCashFlowDataManager(Dao<CashFlow, Long> cashFlowDao, Dao<Wallet, Long> walletDao) {
        this.cashFlowDao = cashFlowDao;
        this.walletDao = walletDao;
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
            return cashFlowDao.queryForAll();
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
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void validateUpdate(CashFlow old, CashFlow newValue) {
        //TODO: if exists
    }

    @Override
    public Long insert(CashFlow cashFlow) {
        try {
            validateInsert(cashFlow);
            cashFlowDao.create(cashFlow);
            fixCurrentAmountInWalletAfterInsert(cashFlow);
            return cashFlow.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void fixCurrentAmountInWalletAfterInsert(CashFlow cashFlow) {
        try {
            Wallet fromWallet = cashFlow.getFromWallet();
            Double newFromWalletCurrentAmount = fromWallet.getCurrentAmount();
            if (fromWallet.getType().equals(Wallet.Type.MY_WALLET)) {
                newFromWalletCurrentAmount -= cashFlow.getAmount();
            } else if (fromWallet.getType().equals(Wallet.Type.CONTRACTOR)) {
                newFromWalletCurrentAmount += cashFlow.getAmount();
            }
            walletDao.update(fromWallet.setCurrentAmount(newFromWalletCurrentAmount));

            Wallet toWallet = cashFlow.getToWallet();
            Double newToWalletCurrentAmount = toWallet.getCurrentAmount();
            if (toWallet.getType().equals(Wallet.Type.MY_WALLET)) {
                newToWalletCurrentAmount += cashFlow.getAmount();
            } else if (toWallet.getType().equals(Wallet.Type.CONTRACTOR)) {
                newToWalletCurrentAmount -= cashFlow.getAmount();
            }
            walletDao.update(toWallet.setCurrentAmount(newToWalletCurrentAmount));

        } catch (SQLException e) {
            e.printStackTrace();
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
}
