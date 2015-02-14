package info.korzeniowski.walletplus.service;

import java.util.List;

import info.korzeniowski.walletplus.model.Wallet;

public interface WalletService extends BaseService<Wallet> {
    public static final Long WALLET_NULL_ID = -1L;

    public List<Wallet> getMyWallets();

    public long countDependentCashFlows(Long walletId);
}
