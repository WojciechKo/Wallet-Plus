package info.korzeniowski.walletplus.datamanager.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(Object entity, Long id) {
        super("Entity:" + entity.getClass().getName() + " with id:" + id + ", already exists in database.");
    }
}
