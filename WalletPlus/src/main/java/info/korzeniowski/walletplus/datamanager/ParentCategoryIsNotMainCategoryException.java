package info.korzeniowski.walletplus.datamanager;

/**
 * Created by Wojtek on 24.03.14.
 */
public class ParentCategoryIsNotMainCategoryException extends RuntimeException {
    public ParentCategoryIsNotMainCategoryException(Long id, Long parentId) {
        super("For Category(id:" + id + ", incorrect parentId:" + parentId +  ") parent category is not Main Category.");
    }
}
