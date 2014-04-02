package info.korzeniowski.walletplus.datamanager;

/**
 * Created by Wojtek on 24.03.14.
 */
public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(Object entity, Long id) {
        super("Entity:" + entity.getClass().getName() + " with id:" + id + ", already exists in database.");
    }
}
