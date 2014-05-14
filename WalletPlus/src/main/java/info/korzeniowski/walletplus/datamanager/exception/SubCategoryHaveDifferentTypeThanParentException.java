package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

/**
 * Created by wojciechko on 13.05.14.
 */
public class SubCategoryHaveDifferentTypeThanParentException extends ValidationException{
    public SubCategoryHaveDifferentTypeThanParentException(String detailMessage) {
        super(detailMessage);
    }
}
