package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class MainCategoryShouldHaveTypeException extends ValidationException{
    public MainCategoryShouldHaveTypeException(String detailMessage) {
        super(detailMessage);
    }
}
