package info.korzeniowski.walletplus.service;

import android.support.v4.util.Pair;

import org.joda.time.Period;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Childable;
import info.korzeniowski.walletplus.model.Identityable;

public interface CategoryService extends BaseService<Category> {

    Category findByName(String name);

    List<Category> getMainCategories();

    List<Category> getMainIncomeTypeCategories();

    List<Category> getMainExpenseTypeCategories();

    List<Category> getSubCategoriesOf(final Long id);

    void deleteByIdWithSubcategories(final Long id);

    public CategoryStats getCategoryStats(Category category, Date firstDay, Period period, Integer iteration);

    public List<CategoryStats> getCategoryStateList(Date firstDay, Period period, Integer iteration);

    public class CategoryStats {
        private Long categoryId;
        private Double flow;
        private Double difference;

        public CategoryStats(Long categoryId) {
            this.categoryId = categoryId;
            flow = 0.0;
            difference = 0.0;
        }

        public CategoryStats(Long categoryId, Double flow, Double difference) {
            this.categoryId = categoryId;
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

        public Long getCategoryId() {
            return categoryId;
        }

        public Double getFlow() {
            return flow;
        }

        public Double getDifference() {
            return difference;
        }
    }
}
