package info.korzeniowski.walletplus.service;

import info.korzeniowski.walletplus.model.Wallet;

public interface WalletService extends BaseService<Wallet> {
    public static final Long WALLET_NULL_ID = -1L;

    public long countDependentCashFlows(Long walletId);
}
