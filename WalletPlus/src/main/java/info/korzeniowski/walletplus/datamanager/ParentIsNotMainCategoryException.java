package info.korzeniowski.walletplus.datamanager;

/**
 * Created by Wojtek on 24.03.14.
 */
public class ParentIsNotMainCategoryException extends RuntimeException {
    public ParentIsNotMainCategoryException(Long id, Long parentId) {
        super("For Category(id:" + id + ", incorrect parentId:" + parentId +  ") parent is not Main Category.");
    }
}
