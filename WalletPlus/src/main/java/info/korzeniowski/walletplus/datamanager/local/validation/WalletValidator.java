package info.korzeniowski.walletplus.datamanager.local.validation;

import com.google.common.base.Objects;

import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.exception.EntityPropertyCannotBeEmptyException;
import info.korzeniowski.walletplus.datamanager.exception.WalletNameAndTypeMustBeUniqueException;
import info.korzeniowski.walletplus.datamanager.exception.WalletTypeCannotBeChangedException;
import info.korzeniowski.walletplus.model.Wallet;

public class WalletValidator implements Validator<Wallet>{
    private final WalletDataManager walletDataManager;

    public WalletValidator(WalletDataManager walletDataManager) {
        this.walletDataManager = walletDataManager;
    }

    @Override
    public void validateInsert(Wallet wallet) {
        validateIfTypeIsNotNull(wallet);
        if (wallet.getType().equals(Wallet.Type.MY_WALLET)) {
            validateIfInitialAmountIsNotNull(wallet);
        }
        validateIfNameAndTypeAreUnique(wallet);
    }

    private void validateIfTypeIsNotNull(Wallet wallet) {
        if (wallet.getType() == null) {
            throw new EntityPropertyCannotBeEmptyException(wallet.getClass().getSimpleName(), "Type");
        }
    }

    private void validateIfInitialAmountIsNotNull(Wallet wallet) {
        if (wallet.getInitialAmount() == null) {
            throw new EntityPropertyCannotBeEmptyException(wallet.getClass().getSimpleName(), "InitialAmount");
        }
    }

    private void validateIfNameAndTypeAreUnique(Wallet wallet) {
        if (walletDataManager.findByNameAndType(wallet.getName(), wallet.getType()) != null) {
            throw new WalletNameAndTypeMustBeUniqueException(wallet.getName());
        }
    }

    @Override
    public void validateUpdate(Wallet newWallet, Wallet oldWallet) {
        validateIfNewNameIsUnique(newWallet, oldWallet);
        validateIfWalletTypeNotChanged(newWallet, oldWallet);
    }

    private void validateIfNewNameIsUnique(Wallet newValue, Wallet toUpdate) {
        if (!(Objects.equal(newValue.getName(), toUpdate.getName()) &&
                Objects.equal(newValue.getType(), toUpdate.getType()))) {
            validateIfNameAndTypeAreUnique(newValue);
        }
    }

    private void validateIfWalletTypeNotChanged(Wallet newWallet, Wallet oldWallet) {
        if (!Objects.equal(newWallet.getType(), oldWallet.getType())) {
            throw new WalletTypeCannotBeChangedException();
        }
    }

    @Override
    public void validateDelete(Wallet wallet) {

    }
}
