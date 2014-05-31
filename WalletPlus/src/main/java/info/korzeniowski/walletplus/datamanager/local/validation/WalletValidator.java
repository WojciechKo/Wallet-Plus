package info.korzeniowski.walletplus.datamanager.local.validation;

import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.exception.WalletTypeCannotBeChangedException;
import info.korzeniowski.walletplus.model.Wallet;

public class WalletValidator implements Validator<Wallet>{
    private final WalletDataManager walletDataManager;

    public WalletValidator(WalletDataManager walletDataManager) {
        this.walletDataManager = walletDataManager;
    }

    @Override
    public void validateInsert(Wallet wallet) {

    }

    @Override
    public void validateUpdate(Wallet newWallet, Wallet oldWallet) {
        if (!newWallet.getType().equals(oldWallet.getType())) {
            throw new WalletTypeCannotBeChangedException();
        }
    }

    @Override
    public void validateDelete(Wallet wallet) {

    }
}
