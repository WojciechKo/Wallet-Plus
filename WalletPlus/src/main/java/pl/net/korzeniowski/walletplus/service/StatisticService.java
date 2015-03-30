package pl.net.korzeniowski.walletplus.service;

import org.joda.time.Period;

import java.util.Date;

import pl.net.korzeniowski.walletplus.model.Tag;

public interface StatisticService {

    Long countCashFlowsAssignedToWallet(Long walletId);

    Long countCashFlowsAssignedToTag(Long tagId);

    TagStats getTagStats(Tag tag, Date firstDay, Period period, Integer iteration);

    class TagStats {
        private final Long tagId;
        private Double income;
        private Double expense;

        public TagStats(Long tagId) {
            this.tagId = tagId;
            income = 0.0;
            expense = 0.0;

        }

        public void incomeAmount(Double amount) {
            this.income += amount;
        }

        public void expanseAmount(Double amount) {
            this.expense += amount;
        }

        public Long getTagId() {
            return tagId;
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

            TagStats that = (TagStats) o;

            if (tagId != null ? !tagId.equals(that.tagId) : that.tagId != null)
                return false;
            if (expense != null ? !expense.equals(that.expense) : that.expense != null)
                return false;
            if (income != null ? !income.equals(that.income) : that.income != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = tagId != null ? tagId.hashCode() : 0;
            result = 31 * result + (income != null ? income.hashCode() : 0);
            result = 31 * result + (expense != null ? expense.hashCode() : 0);
            return result;
        }
    }
}
