package info.korzeniowski.walletplus.service;

import android.util.Pair;

import org.joda.time.Period;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;

public interface CategoryService extends BaseService<Category> {

    Category findByName(String name);

    List<Category> getMainCategories();

    List<Category> getMainIncomeTypeCategories();

    List<Category> getMainExpenseTypeCategories();

    List<Category> getSubCategoriesOf(final Long id);

    void deleteByIdWithSubcategories(final Long id);

    public CategoryStats getCategoryStats(Category category, Date firstDay, Period period, Integer iteration);

    public List<Pair<Category, CategoryStats>> getCategoryListWithStats(Date firstDay, Period period, Integer iteration);

    public class CategoryStats {
        private Double flow;
        private Double difference;

        public CategoryStats() {
            flow = 0.0;
            difference = 0.0;
        }

        public CategoryStats(Double flow, Double difference) {
            this.flow = flow;
            this.difference = difference;
        }

        public void incomeAmount(Double amount) {
            this.flow += amount;
            this.difference += amount;
        }

        public void expanseAmount(Double amount) {
            this.flow += amount;
            this.difference -= amount;
        }

        public Double getFlow() {
            return flow;
        }

        public Double getDifference() {
            return difference;
        }
    }
}
