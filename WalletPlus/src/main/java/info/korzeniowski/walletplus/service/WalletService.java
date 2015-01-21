package info.korzeniowski.walletplus.service;

import java.util.List;

import info.korzeniowski.walletplus.model.Wallet;

public interface WalletService extends BaseService<Wallet> {
    public List<Wallet> getMyWallets();

    public List<Wallet> getContractors();

    long countDependentCashFlows(Long walletId);
}
