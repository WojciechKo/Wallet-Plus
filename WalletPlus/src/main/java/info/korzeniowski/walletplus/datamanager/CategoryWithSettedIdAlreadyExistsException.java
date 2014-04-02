package info.korzeniowski.walletplus.datamanager;

/**
 * Created by Wojtek on 24.03.14.
 */
public class CategoryWithSettedIdAlreadyExistsException extends RuntimeException {
    CategoryWithSettedIdAlreadyExistsException(Long id) {
        super("Category with id: " + id + " already exists.");
    }
}
