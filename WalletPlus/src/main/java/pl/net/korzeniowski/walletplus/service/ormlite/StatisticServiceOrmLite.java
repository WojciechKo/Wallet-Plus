package pl.net.korzeniowski.walletplus.service.ormlite;

import com.j256.ormlite.dao.Dao;

import org.joda.time.Period;

import java.sql.SQLException;
import java.util.Date;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.model.TagAndCashFlowBind;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.service.StatisticService;
import pl.net.korzeniowski.walletplus.service.exception.DatabaseException;

public class StatisticServiceOrmLite implements StatisticService {

    private Dao<CashFlow, Long> cashFlowDao;
    private Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindDao;
    private CashFlowService cashFlowService;

    @Inject
    public StatisticServiceOrmLite(
            Dao<CashFlow, Long> cashFlowDao,
            Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindDao,
            CashFlowService cashFlowService) {

        this.cashFlowDao = cashFlowDao;
        this.tagAndCashFlowBindDao = tagAndCashFlowBindDao;
        this.cashFlowService = cashFlowService;
    }

    @Override
    public Long countCashFlowsAssignedToWallet(Long walletId) {
        try {
            return cashFlowDao.queryBuilder().where().eq(CashFlow.WALLET_ID_COLUMN_NAME, walletId).countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Long countCashFlowsAssignedToTag(Long tagId) {
        try {
            return tagAndCashFlowBindDao.queryBuilder().where().eq(TagAndCashFlowBind.TAG_ID_COLUMN_NAME, tagId).countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public TagStats getTagStats(Tag tag, final Date firstDay, final Period period, final Integer iteration) {
        throw new RuntimeException("Not implemented.");
//        checkNotNull(tag);
//        checkNotNull(firstDay);
//        checkNotNull(period);
//        checkNotNull(iteration);
//
//        DateTime firstDayArg;
//        if (iteration <= 0) {
//            firstDayArg = new DateTime(firstDay).minus(period.multipliedBy(0 - iteration));
//        } else {
//            firstDayArg = new DateTime(firstDay).plus(period.multipliedBy(iteration));
//        }
//
//        DateTime lastDayArg = firstDayArg.plus(period);
//        List<CashFlow> cashFlowList = cashFlowService.findCashFlows(firstDayArg.toDate(), lastDayArg.toDate(), tag.getId(), null);
//
//        TagStats stats = new TagStats(tag.getId());
//        for (CashFlow cashFlow : cashFlowList) {
//            CashFlow.Type type = cashFlow.getType();
//            if (type == CashFlow.Type.INCOME) {
//                stats.incomeAmount(cashFlow.getAmount());
//            } else if (type == CashFlow.Type.EXPANSE) {
//                stats.expanseAmount(cashFlow.getAmount());
//            }
//        }
//
//        return stats;
    }
}
