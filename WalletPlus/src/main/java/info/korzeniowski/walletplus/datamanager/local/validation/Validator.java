package info.korzeniowski.walletplus.datamanager.local.validation;

public interface Validator<E> {
    public void validateInsert(E entity);

    public void validateDelete(Long id);

    public void validateUpdate(E newEntity);
}
