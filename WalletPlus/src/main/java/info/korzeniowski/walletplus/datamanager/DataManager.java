package info.korzeniowski.walletplus.datamanager;

import java.util.List;

/**
 * Created by Wojtek on 04.03.14.
 */
public interface DataManager<E> {

    Long count();

    E getById(Long id);

    List<E> getAll();

    void update(E entity);

    Long insert(E entity);

    void deleteById(Long id);
}
