package info.korzeniowski.walletplus.datamanager.exception;

public class CategoryWithGivenNameAlreadyExistsException extends RuntimeException {
    public CategoryWithGivenNameAlreadyExistsException(String msg) {
        super("Category with name: " + msg + " already exists.");
    }
}
