package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryWithGivenIdAlreadyExistsException extends ValidationException{
    public CategoryWithGivenIdAlreadyExistsException(String detailMessage) {
        super(detailMessage);
    }
}
