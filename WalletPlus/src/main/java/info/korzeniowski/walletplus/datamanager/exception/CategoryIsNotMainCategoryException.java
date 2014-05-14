package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.datamanager.ValidationException;

public class CategoryIsNotMainCategoryException extends ValidationException {
    public CategoryIsNotMainCategoryException(Long id, Long parentId) {
        super("For Category(id:" + id + ", incorrect parentId:" + parentId +  ") parent is not Main Category.");
    }
}
