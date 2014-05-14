package info.korzeniowski.walletplus.datamanager;

public class ValidationException extends RuntimeException {
    public ValidationException(String detailMessage) {
        super(detailMessage);
    }
}
