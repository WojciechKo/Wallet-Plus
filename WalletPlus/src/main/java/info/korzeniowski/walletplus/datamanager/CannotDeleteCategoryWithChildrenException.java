package info.korzeniowski.walletplus.datamanager;

import info.korzeniowski.walletplus.model.Category;

/**
 * Created by Wojtek on 11.03.14.
 */
public class CannotDeleteCategoryWithChildrenException extends RuntimeException {
    public CannotDeleteCategoryWithChildrenException(Category category) {
        super("Category: " + category.getName() + "have children, cannot delete.");
    }
}
