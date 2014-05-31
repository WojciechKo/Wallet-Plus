package info.korzeniowski.walletplus.datamanager.local.validation;

public interface Validator<E> {
    public void validateInsert(E entity);
    public void validateUpdate(E newEntity, E oldEntity);
    public void validateDelete(E entity);
}
