package pl.net.korzeniowski.walletplus.service.exception;

public class EntityPropertyCannotBeNullOrEmptyException extends RuntimeException {
    private final String entity;
    private final String property;

    public EntityPropertyCannotBeNullOrEmptyException(String entity, String property) {
        super ("Property: " + property + " of Entity: " + entity + ", can't be null.");
        this.entity = entity;
        this.property = property;
    }

    public String getEntity() {
        return entity;
    }

    public String getProperty() {
        return property;
    }
}
