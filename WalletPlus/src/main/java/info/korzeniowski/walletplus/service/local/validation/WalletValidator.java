package info.korzeniowski.walletplus.service.local.validation;

import com.google.common.base.Objects;

import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.exception.WalletNameAndTypeMustBeUniqueException;
import info.korzeniowski.walletplus.service.exception.WalletTypeCannotBeChangedException;
import info.korzeniowski.walletplus.model.Wallet;

import static com.google.common.base.Preconditions.checkNotNull;

public class WalletValidator implements Validator<Wallet>{
    private final WalletService walletService;

    public WalletValidator(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public void validateInsert(Wallet wallet) {
        checkNotNull(wallet);
        validateIfTypeIsNotNull(wallet);
        if (wallet.getType().equals(Wallet.Type.MY_WALLET)) {
            validateIfInitialAmountIsNotNull(wallet);
        }
        validateIfNameAndTypeAreUnique(wallet);
    }

    private void validateIfTypeIsNotNull(Wallet wallet) {
        if (wallet.getType() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(wallet.getClass().getSimpleName(), "Type");
        }
    }

    private void validateIfInitialAmountIsNotNull(Wallet wallet) {
        if (wallet.getInitialAmount() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(wallet.getClass().getSimpleName(), "InitialAmount");
        }
    }

    private void validateIfNameAndTypeAreUnique(Wallet wallet) {
        Wallet found = walletService.findByNameAndType(wallet.getName(), wallet.getType());
        if (found != null) {
            throw new WalletNameAndTypeMustBeUniqueException(wallet.getName());
        }
    }

    @Override
    public void validateUpdate(Wallet newWallet) {
        Wallet oldWallet = walletService.findById(newWallet.getId());
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
    public void validateDelete(Long id) {

    }
}
