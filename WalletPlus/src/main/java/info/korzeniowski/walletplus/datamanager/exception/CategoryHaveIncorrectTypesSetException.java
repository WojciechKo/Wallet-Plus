package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

/**
 * Created by wojciechko on 13.05.14.
 */
public class CategoryHaveIncorrectTypesSetException extends ValidationException{
    public CategoryHaveIncorrectTypesSetException(String detailMessage) {
        super(detailMessage);
    }
}
