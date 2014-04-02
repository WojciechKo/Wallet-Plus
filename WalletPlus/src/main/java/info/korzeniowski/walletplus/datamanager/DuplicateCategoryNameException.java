package info.korzeniowski.walletplus.datamanager;

/**
 * Created by Wojtek on 02.04.14.
 */
public class DuplicateCategoryNameException extends RuntimeException {
    public DuplicateCategoryNameException(String msg) {
        super("Category with name: " + msg + " already exists.");
    }
}
