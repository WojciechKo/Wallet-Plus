package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryWithGivenNameAlreadyExistsException extends ValidationException {
    public CategoryWithGivenNameAlreadyExistsException(String msg) {
        super("Category with name: " + msg + " already exists.");
    }
}
