package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryIsNotMainCategoryException extends ValidationException {

    public CategoryIsNotMainCategoryException(String detailMessage) {
        super(detailMessage);
    }
}
