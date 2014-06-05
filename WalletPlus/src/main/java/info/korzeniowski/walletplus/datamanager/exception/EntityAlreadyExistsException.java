package info.korzeniowski.walletplus.datamanager.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String entity, Long id) {
        super("Entity:" + entity + " with id:" + id + ", already exists in database.");
    }
}
