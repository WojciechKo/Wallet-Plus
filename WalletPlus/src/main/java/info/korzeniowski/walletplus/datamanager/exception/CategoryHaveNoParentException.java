package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryHaveNoParentException extends ValidationException{
    public CategoryHaveNoParentException(String detailMessage) {
        super(detailMessage);
    }
}
