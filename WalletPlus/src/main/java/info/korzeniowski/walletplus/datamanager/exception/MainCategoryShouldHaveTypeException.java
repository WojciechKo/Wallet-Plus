package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

/**
 * Created by wojciechko on 14.05.14.
 */
public class MainCategoryShouldHaveTypeException extends ValidationException{
    public MainCategoryShouldHaveTypeException(String detailMessage) {
        super(detailMessage);
    }
}
