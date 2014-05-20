package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class SubCategoryHaveDifferentTypeThanParentException extends ValidationException{
    public SubCategoryHaveDifferentTypeThanParentException(String detailMessage) {
        super(detailMessage);
    }
}
