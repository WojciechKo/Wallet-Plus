package info.korzeniowski.walletplus.service;

import java.util.List;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;

public interface TagService extends BaseService<Tag> {
    Tag findByName(String name);

    List<Tag> getAll();

    public List<CashFlow> getAssociatedCashFlows(Long tagid, Long n);

    long countDependentCashFlows(Long tagId);
}
