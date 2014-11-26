package info.korzeniowski.walletplus.service;

import org.joda.time.Period;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;

public interface CategoryService extends BaseService<Category> {

    Category findByName(String name);

    List<Category> getMainCategories();

    List<Category> getSubCategoriesOf(final Long id);

    void deleteByIdWithSubcategories(final Long id);

    public CategoryStats getCategoryStats(Category category, Date firstDay, Period period, Integer iteration);

    public List<CategoryStats> getCategoryStatsList(Date firstDay, Period period, Integer iteration);

    public class CategoryStats {
        private Long categoryId;
        private Double flow;
        private Double totalFlow;
        private Double difference;
        private Double totalDifference;

        public CategoryStats(Long categoryId) {
            this.categoryId = categoryId;
            flow = 0.0;
            totalFlow = 0.0;
            difference = 0.0;
            totalDifference = 0.0;
        }

        public void incomeAmount(Double amount) {
            this.flow += amount;
            this.totalFlow += amount;
            this.difference += amount;
            this.totalDifference += amount;
        }

        public void incomeAmountFromSub(Double amount) {
            this.totalFlow += amount;
            this.totalDifference += amount;
        }

        public void expanseAmount(Double amount) {
            this.flow += amount;
            this.difference -= amount;
            this.totalFlow += amount;
            this.totalDifference -= amount;
        }

        public void expanseAmountFromSub(Double amount) {
            this.totalFlow += amount;
            this.totalDifference -= amount;
        }

        public void includeSubCategoryStats(CategoryStats subCategoryStats) {
            this.totalFlow += subCategoryStats.getFlow();
            this.totalDifference += subCategoryStats.getDifference();
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public Double getFlow() {
            return flow;
        }

        public Double getTotalFlow() {
            return totalFlow;
        }

        public Double getDifference() {
            return difference;
        }

        public Double getTotalDifference() {
            return totalDifference;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CategoryStats that = (CategoryStats) o;

            if (categoryId != null ? !categoryId.equals(that.categoryId) : that.categoryId != null)
                return false;
            if (difference != null ? !difference.equals(that.difference) : that.difference != null)
                return false;
            if (flow != null ? !flow.equals(that.flow) : that.flow != null) return false;
            if (totalDifference != null ? !totalDifference.equals(that.totalDifference) : that.totalDifference != null)
                return false;
            if (totalFlow != null ? !totalFlow.equals(that.totalFlow) : that.totalFlow != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = categoryId != null ? categoryId.hashCode() : 0;
            result = 31 * result + (flow != null ? flow.hashCode() : 0);
            result = 31 * result + (totalFlow != null ? totalFlow.hashCode() : 0);
            result = 31 * result + (difference != null ? difference.hashCode() : 0);
            result = 31 * result + (totalDifference != null ? totalDifference.hashCode() : 0);
            return result;
        }
    }
}
