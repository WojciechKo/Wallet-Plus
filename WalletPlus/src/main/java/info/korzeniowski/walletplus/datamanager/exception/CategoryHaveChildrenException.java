package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryHaveChildrenException extends ValidationException{
    public CategoryHaveChildrenException(String detailMessage) {
        super(detailMessage);
    }
}
