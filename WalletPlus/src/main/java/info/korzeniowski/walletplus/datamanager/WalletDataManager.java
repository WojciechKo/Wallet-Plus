package info.korzeniowski.walletplus.datamanager;

import java.util.List;

import info.korzeniowski.walletplus.model.Wallet;

public interface WalletDataManager extends DataManager<Wallet>{
    public List<Wallet> getMyWallets();
    public List<Wallet> getContractors();
}
