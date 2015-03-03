package info.korzeniowski.walletplus.service.ormlite.validation;

interface Validator<E> {
    public void validateInsert(E entity);

    public void validateDelete(Long id);

    public void validateUpdate(E newEntity);
}
