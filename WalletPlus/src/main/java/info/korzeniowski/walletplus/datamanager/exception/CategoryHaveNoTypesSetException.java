package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryHaveNoTypesSetException extends ValidationException{
    public CategoryHaveNoTypesSetException(String detailMessage) {
        super(detailMessage);
    }
}
