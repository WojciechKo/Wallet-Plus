package info.korzeniowski.walletplus.service;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.CashFlow;

public interface CashFlowService extends BaseService<CashFlow> {
    public long countAssignedWithTag(Long tagId);

    public List<CashFlow> findCashFlow(Date from, Date to, Long tagId, Long walletId);

    List<CashFlow> getLastNCashFlows(int n);
}
