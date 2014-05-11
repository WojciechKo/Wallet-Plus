package info.korzeniowski.walletplus.datamanager.exception;

public class ParentIsNotMainCategoryException extends RuntimeException {
    public ParentIsNotMainCategoryException(Long id, Long parentId) {
        super("For Category(id:" + id + ", incorrect parentId:" + parentId +  ") parent is not Main Category.");
    }
}
