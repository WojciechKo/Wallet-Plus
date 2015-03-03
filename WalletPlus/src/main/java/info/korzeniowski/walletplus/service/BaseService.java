package info.korzeniowski.walletplus.service;

import java.util.List;

public interface BaseService<E> {
    public static final String ORMLITE_IMPL = "ormlite";

    Long insert(E entity);

    Long count();

    E findById(Long id);

    List<E> getAll();

    void update(E entity);

    void deleteById(Long id);
}
