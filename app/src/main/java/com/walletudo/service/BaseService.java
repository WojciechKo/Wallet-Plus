package com.walletudo.service;

import java.util.List;

public interface BaseService<E> {

    Long insert(E entity);

    Long count();

    E findById(Long id);

    List<E> getAll();

    void update(E entity);

    void deleteById(Long id);
}
