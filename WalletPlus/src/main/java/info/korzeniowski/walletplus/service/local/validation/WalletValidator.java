package info.korzeniowski.walletplus.service.local.validation;

import com.google.common.base.Objects;

import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.exception.WalletTypeCannotBeChangedException;

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
            wallet.setCurrentAmount(wallet.getInitialAmount());
        }
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

    @Override
    public void validateUpdate(Wallet newWallet) {
        Wallet oldWallet = walletService.findById(newWallet.getId());
        validateIfWalletTypeNotChanged(newWallet, oldWallet);
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
