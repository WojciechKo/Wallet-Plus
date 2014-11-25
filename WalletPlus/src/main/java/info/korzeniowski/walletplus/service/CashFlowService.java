package info.korzeniowski.walletplus.service;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;

public interface CashFlowService extends BaseService<CashFlow> {
    long countAssignedToWallet(Long walletId);

    public Category getTransferCategory();

    public List<CashFlow> findCashFlow(Date from, Date to, Long categoryId, Long fromWalletId, Long toWalletId);

    public List<CashFlow> findCashFlow(Date from, Date to, Category.Type categoryType, Long fromWalletId, Long toWalletId);
}
