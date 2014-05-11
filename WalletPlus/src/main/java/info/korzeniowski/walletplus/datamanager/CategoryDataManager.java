package info.korzeniowski.walletplus.datamanager;

import java.util.List;

import info.korzeniowski.walletplus.model.Category;


/**
 * Created by Wojtek on 04.03.14.
 */
public interface CategoryDataManager extends DataManager<Category> {

    List<Category> getMainCategories();

    List<Category> getMainIncomeTypeCategories();

    List<Category> getMainExpenseTypeCategories();

    void deleteByIdWithSubcategories(final Long id);
}
