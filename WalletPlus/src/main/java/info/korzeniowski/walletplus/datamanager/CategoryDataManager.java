package info.korzeniowski.walletplus.datamanager;

import java.util.List;

import info.korzeniowski.walletplus.model.Category;

public interface CategoryDataManager extends DataManager<Category> {

    Category findByName(String name);

    List<Category> getMainCategories();

    List<Category> getMainIncomeTypeCategories();

    List<Category> getMainExpenseTypeCategories();

    void deleteByIdWithSubcategories(final Long id);
}
