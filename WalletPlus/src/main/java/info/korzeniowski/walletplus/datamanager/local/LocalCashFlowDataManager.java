package info.korzeniowski.walletplus.datamanager.local;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.greendao.GreenCashFlow;
import info.korzeniowski.walletplus.model.greendao.GreenCashFlowDao;

public class LocalCashFlowDataManager implements CashFlowDataManager {
    private final GreenCashFlowDao greenCashFlowDao;
    private final List<CashFlow> cashFlows;


    @Inject
    public LocalCashFlowDataManager(GreenCashFlowDao greenCashFlowDao) {
        this.greenCashFlowDao = greenCashFlowDao;
        cashFlows = getAll();
    }

    @Override
    public Long count() {
        return (long) cashFlows.size();
    }

    @Override
    public CashFlow findById(final Long id) {
        Preconditions.checkNotNull(id);

        return Iterables.find(cashFlows, new Predicate<CashFlow>() {
            @Override
            public boolean apply(CashFlow cashFlow) {
                return Objects.equal(cashFlow.getId(), id);
            }
        });
    }

    @Override
    public List<CashFlow> getAll() {
        return getCategoryListFromGreenCategoryList(greenCashFlowDao.loadAll());
    }

    private List<CashFlow> getCategoryListFromGreenCategoryList(List<GreenCashFlow> greenCashFlows) {
        List<CashFlow> cashFlowList = new ArrayList<CashFlow>();
        for(GreenCashFlow greenCashFlow : greenCashFlows) {
            cashFlowList.add(GreenCashFlow.toCashFlow(greenCashFlow));
        }
        return cashFlowList;
    }

    @Override
    public void update(CashFlow cashFlow) {
        validateUpdate(cashFlow);
        CashFlow toUpdate = findById(cashFlow.getId());
        toUpdate.setAmount(cashFlow.getAmount());
        toUpdate.setCategoryId(cashFlow.getCategoryId());
        toUpdate.setComment(cashFlow.getComment());
        toUpdate.setDateTime(cashFlow.getDateTime());
        greenCashFlowDao.update(new GreenCashFlow(cashFlow));
    }

    private void validateUpdate(CashFlow cashFlow) {
    }

    @Override
    public Long insert(CashFlow entity) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
