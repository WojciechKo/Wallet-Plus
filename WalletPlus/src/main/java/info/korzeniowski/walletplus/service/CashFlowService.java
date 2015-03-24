package info.korzeniowski.walletplus.service;

import com.google.common.collect.Sets;

import java.util.Date;
import java.util.List;
import java.util.Set;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;

public interface CashFlowService extends BaseService<CashFlow> {
    public List<CashFlow> findCashFlows(CashFlowQuery query);

    List<CashFlow> getLastNCashFlows(int n);

    public static class CashFlowQuery {
        private Long walletId;
        private Double minAmount;
        private Double maxAmount;
        private Date fromDate;
        private Date toDate;
        private Set<Tag> withTagSet;
        private Set<Tag> withoutTagSet;

        public CashFlowQuery() {
            withTagSet = Sets.newHashSet();
            withoutTagSet = Sets.newHashSet();
        }

        public CashFlowQuery withWalletId(Long walletId) {
            this.walletId = walletId;
            return this;
        }

        public Long getWalletId() {
            return walletId;
        }

        public CashFlowQuery withMinAmount(Double minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public Double getMinAmount() {
            return minAmount;
        }

        public CashFlowQuery withMaxAmount(Double maxAmount) {
            this.maxAmount = maxAmount;
            return this;
        }

        public Double getMaxAmount() {
            return maxAmount;
        }

        public CashFlowQuery withFromDate(Date fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Date getFromDate() {
            return fromDate;
        }

        public CashFlowQuery withToDate(Date toDate) {
            this.toDate = toDate;
            return this;
        }

        public Date getToDate() {
            return toDate;
        }

        public CashFlowQuery withTags(Tag... tags) {
            this.withTagSet = Sets.newHashSet(tags);
            return this;
        }

        public Set<Tag> getWithTagSet() {
            return withTagSet;
        }

        public CashFlowQuery withoutTags(Tag... tags) {
            this.withoutTagSet = Sets.newHashSet(tags);
            return this;
        }

        public Set<Tag> getWithoutTagSet() {
            return withoutTagSet;
        }
    }
}
