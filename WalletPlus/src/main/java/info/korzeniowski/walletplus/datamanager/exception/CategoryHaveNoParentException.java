package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

/**
 * Created by wojciechko on 14.05.14.
 */
public class CategoryHaveNoParentException extends ValidationException{
    public CategoryHaveNoParentException(String detailMessage) {
        super(detailMessage);
    }
}
