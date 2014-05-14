package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryHaveParentException extends ValidationException {
    public CategoryHaveParentException(String detailMessage) {
        super(detailMessage);
    }
}
