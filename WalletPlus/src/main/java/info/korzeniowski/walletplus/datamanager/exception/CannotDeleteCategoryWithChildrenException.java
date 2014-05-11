package info.korzeniowski.walletplus.datamanager.exception;

import info.korzeniowski.walletplus.model.Category;

public class CannotDeleteCategoryWithChildrenException extends RuntimeException {
    public CannotDeleteCategoryWithChildrenException(Category category) {
        super("Category: " + category.getName() + "have children, cannot delete.");
    }
}
