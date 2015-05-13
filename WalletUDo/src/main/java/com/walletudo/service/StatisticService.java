package com.walletudo.service;

import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Tag;
import com.walletudo.util.KorzeniowskiUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StatisticService {

    Long countCashFlowsAssignedToWallet(Long walletId);

    Long countCashFlowsAssignedToTag(Long tagId);

    Statistics getStatistics(Date firstDay, Date lastDay);

    Map<Tag, TagStats2> getTagStats2(Tag tag);

    class TagStats2 {
        private Double income = 0.0;
        private Double expense = 0.0;

        public TagStats2(Double income, Double expense) {
            this.income = income;
            this.expense = expense;
        }

        public Double getIncome() {
            return income;
        }

        public Double getExpense() {
            return expense;
        }
    }

    class Statistics {
        Map<Tag, Double> profitMap;
        Map<Tag, Double> lostMap;
        Map<Tag, Double> incomeMap;
        Map<Tag, Double> expenseMap;

        public Statistics(Set<TagStats> tagStatsSet) {
            final List<Map.Entry<Tag, Double>> incomeList = Lists.newArrayList();
            List<Map.Entry<Tag, Double>> expenseList = Lists.newArrayList();
            List<Map.Entry<Tag, Double>> profitList = Lists.newArrayList();
            List<Map.Entry<Tag, Double>> lostList = Lists.newArrayList();

            for (TagStats tagStats : tagStatsSet) {
                incomeList.add(Maps.immutableEntry(tagStats.getTag(), tagStats.getIncome()));
                expenseList.add(Maps.immutableEntry(tagStats.getTag(), tagStats.getExpense()));
                double profit = tagStats.getIncome() - tagStats.getExpense();
                if (profit > 0.0) {
                    profitList.add(Maps.immutableEntry(tagStats.getTag(), profit));
                } else if (profit < 0.0) {
                    lostList.add(Maps.immutableEntry(tagStats.getTag(), -profit));
                } else {
                    profitList.add(Maps.immutableEntry(tagStats.getTag(), 0.0));
                    lostList.add(Maps.immutableEntry(tagStats.getTag(), 0.0));
                }
            }

            Comparator<Map.Entry<Tag, Double>> greaterFirstComparator = new Comparator<Map.Entry<Tag, Double>>() {
                @Override
                public int compare(Map.Entry<Tag, Double> lhs, Map.Entry<Tag, Double> rhs) {
                    return rhs.getValue().compareTo(lhs.getValue());
                }
            };

            this.incomeMap = KorzeniowskiUtils.Collections.getMapFromEntryList(FluentIterable.from(incomeList).toSortedList(greaterFirstComparator));
            this.expenseMap = KorzeniowskiUtils.Collections.getMapFromEntryList(FluentIterable.from(expenseList).toSortedList(greaterFirstComparator));
            this.profitMap = KorzeniowskiUtils.Collections.getMapFromEntryList(FluentIterable.from(profitList).toSortedList(greaterFirstComparator));
            this.lostMap = KorzeniowskiUtils.Collections.getMapFromEntryList(FluentIterable.from(lostList).toSortedList(greaterFirstComparator));
        }

        public Map<Tag, Double> getProfit() {
            return profitMap;
        }

        public Map<Tag, Double> getLost() {
            return lostMap;
        }

        public Map<Tag, Double> getIncome() {
            return incomeMap;
        }

        public Map<Tag, Double> getExpense() {
            return expenseMap;
        }
    }

    class TagStats {
        private final Tag tag;
        private Double income;
        private Double expense;

        public TagStats(Tag tag) {
            this.tag = tag;
            income = 0.0;
            expense = 0.0;
        }

        public TagStats(Tag tag, Double income, Double expense) {
            this.tag = tag;
            this.income = income;
            this.expense = expense;
        }

        public void includeCashFlow(CashFlow cashFlow) {
            if (cashFlow.getType().equals(CashFlow.Type.EXPENSE)) {
                expense += cashFlow.getAmount();
            } else if (cashFlow.getType().equals(CashFlow.Type.INCOME)) {
                income += cashFlow.getAmount();
            }
        }

        public Tag getTag() {
            return tag;
        }

        public Double getIncome() {
            return income;
        }

        public Double getExpense() {
            return expense;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TagStats tagStats = (TagStats) o;
            return Objects.equal(tag, tagStats.tag) &&
                    Objects.equal(income, tagStats.income) &&
                    Objects.equal(expense, tagStats.expense);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(tag, income, expense);
        }
    }
}
