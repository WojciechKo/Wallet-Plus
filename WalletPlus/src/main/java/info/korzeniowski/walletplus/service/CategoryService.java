package info.korzeniowski.walletplus.service;

import org.joda.time.Period;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;

public interface CategoryService extends BaseService<Category> {
    public static final Long CATEGORY_NULL_ID = -1L;

    Category findByName(String name);

    List<Category> getAll();

    public CategoryStats getCategoryStats(Category category, Date firstDay, Period period, Integer iteration);

    public List<CategoryStats> getCategoryStatsList(Date firstDay, Period period, Integer iteration);

    long countDependentCashFlows(Long categoryId);

    public class CategoryStats {
        private final Long categoryId;
        private Double income;
        private Double expense;

        public CategoryStats(Long categoryId) {
            this.categoryId = categoryId;
            income = 0.0;
            expense = 0.0;

        }

        public void incomeAmount(Double amount) {
            this.income += amount;
        }

        public void expanseAmount(Double amount) {
            this.expense += amount;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public Double getIncome() {
            return income;
        }

        public Double getExpense() {
            return expense;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            CategoryStats that = (CategoryStats) o;

            if (categoryId != null ? !categoryId.equals(that.categoryId) : that.categoryId != null)
                return false;
            if (expense != null ? !expense.equals(that.expense) : that.expense != null)
                return false;
            if (income != null ? !income.equals(that.income) : that.income != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = categoryId != null ? categoryId.hashCode() : 0;
            result = 31 * result + (income != null ? income.hashCode() : 0);
            result = 31 * result + (expense != null ? expense.hashCode() : 0);
            return result;
        }
    }
}
