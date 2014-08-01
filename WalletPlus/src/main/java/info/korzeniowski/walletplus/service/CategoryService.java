package info.korzeniowski.walletplus.service;

import java.util.List;

import info.korzeniowski.walletplus.model.Category;

public interface CategoryService extends BaseService<Category> {

    Category findByName(String name);

    List<Category> getMainCategories();

    List<Category> getMainIncomeTypeCategories();

    List<Category> getMainExpenseTypeCategories();

    List<Category> getSubCategoriesOf(final Long id);

    void deleteByIdWithSubcategories(final Long id);
}
