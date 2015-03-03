package info.korzeniowski.walletplus.service.ormlite;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.TagService;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatisticServiceOrmLite implements StatisticService {

    @Inject
    @Named(CashFlowService.ORMLITE_IMPL)
    CashFlowService cashFlowService;

    @Inject
    @Named(TagService.ORMLITE_IMPL)
    TagService tagService;

    @Override
    public TagStats getTagStats(Tag tag, final Date firstDay, final Period period, final Integer iteration) {
        checkNotNull(tag);
        checkNotNull(firstDay);
        checkNotNull(period);
        checkNotNull(iteration);

        DateTime firstDayArg;
        if (iteration <= 0) {
            firstDayArg = new DateTime(firstDay).minus(period.multipliedBy(0 - iteration));
        } else {
            firstDayArg = new DateTime(firstDay).plus(period.multipliedBy(iteration));
        }

        DateTime lastDayArg = firstDayArg.plus(period);
        List<CashFlow> cashFlowList = cashFlowService.findCashFlow(firstDayArg.toDate(), lastDayArg.toDate(), tag.getId(), null);

        TagStats stats = new TagStats(tag.getId());
        for (CashFlow cashFlow : cashFlowList) {
            CashFlow.Type type = cashFlow.getType();
            if (type == CashFlow.Type.INCOME) {
                stats.incomeAmount(cashFlow.getAmount());
            } else if (type == CashFlow.Type.EXPANSE) {
                stats.expanseAmount(cashFlow.getAmount());
            }
        }

        return stats;
    }
}
