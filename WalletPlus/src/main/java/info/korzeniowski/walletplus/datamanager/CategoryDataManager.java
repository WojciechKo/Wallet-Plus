package info.korzeniowski.walletplus.datamanager;

import java.util.List;

import info.korzeniowski.walletplus.model.Category;


/**
 * Created by Wojtek on 04.03.14.
 */
public interface CategoryDataManager extends DataManager<Category> {

    void deleteByIdWithSubcategories(Long id);
    List<Category> getMainIncomeCategories();
    List<Category> getMainExpenseCategories();
    List<Category> getMainCategories();
    List<Category> getByMainPosition(int mainPosition);
    Category getByMainAndSubPosition(int mainPosition, int subPosition);
}
