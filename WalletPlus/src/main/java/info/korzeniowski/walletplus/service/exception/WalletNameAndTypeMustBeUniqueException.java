package info.korzeniowski.walletplus.service.exception;

public class WalletNameAndTypeMustBeUniqueException extends RuntimeException {
    public WalletNameAndTypeMustBeUniqueException(String name) {
        super ("Wallet name: " + name + " is not unique.");
    }
}
