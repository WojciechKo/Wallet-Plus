package info.korzeniowski.walletplus.datamanager;

/**
 * Created by Wojtek on 24.03.14.
 */
public class NotFoundInMainCategoriesException extends RuntimeException{
    public NotFoundInMainCategoriesException(Long id) {
        super("Category id: " + id.toString());
    }
}
