package info.korzeniowski.walletplus.datamanager;

import java.util.List;

public interface DataManager<E> {

    Long insert(E entity);

    Long count();

    E getById(Long id);

    List<E> getAll();

    void update(E entity);

    void deleteById(Long id);
}
