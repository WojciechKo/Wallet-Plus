package info.korzeniowski.walletplus.service;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;

public interface CashFlowService extends BaseService<CashFlow> {
    long countAssignedToWallet(Long walletId);

    public Category getOtherCategory();
}
