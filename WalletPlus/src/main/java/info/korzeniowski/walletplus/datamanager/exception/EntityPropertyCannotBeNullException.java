package info.korzeniowski.walletplus.datamanager.exception;

public class EntityPropertyCannotBeNullException extends RuntimeException{
    private String entity;
    private String property;

    public EntityPropertyCannotBeNullException(String entity, String property) {
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
