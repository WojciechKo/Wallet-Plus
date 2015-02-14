package info.korzeniowski.walletplus.service.local;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.validation.TagValidator;
import info.korzeniowski.walletplus.util.KorzeniowskiUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocalTagService implements TagService {
    private final TagValidator tagValidator;
    private final Dao<Tag, Long> tagDao;

    CashFlowService cashFlowService;

    @Inject
    public LocalTagService(Dao<Tag, Long> tagDao) {
        this.tagDao = tagDao;
        this.tagValidator = new TagValidator(this);
    }

    public LocalTagService(Dao<Tag, Long> tagDao, TagValidator tagValidator) {
        this.tagDao = tagDao;
        this.tagValidator = tagValidator;
    }

    /**
     * ********
     * CREATE *
     * ********
     */
    @Override
    public Long insert(Tag tag) {
        try {
            tagValidator.validateInsert(tag);
            tagDao.create(tag);
            return tag.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * ******
     * READ *
     * ******
     */
    @Override
    public Tag findById(final Long id) {
        try {
            return tagDao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Tag findByName(final String name) {
        try {
            return tagDao.queryBuilder().where().eq("name", name).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Tag> getAll() {
        try {
            return tagDao.queryBuilder().orderByRaw("name COLLATE NOCASE").query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Long count() {
        try {
            return tagDao.countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * ********
     * UPDATE *
     * ********
     */
    @Override
    public void update(final Tag newValue) {
        try {
            tagValidator.validateUpdate(newValue);
            tagDao.update(newValue);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * ********
     * DELETE *
     * ********
     */
    @Override
    public void deleteById(Long id) {
        try {
            tagValidator.validateDelete(id);
            tagDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * *************
     * STATISTICS *
     * *************
     */

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

    @Override
    public List<TagStats> getTagStatsList(Date firstDay, Period period, Integer iteration) {
        throw new RuntimeException("Not implemented!");
//        try {
//            List<TagStats> result = createTagStatsResults(tagDao.queryForAll());
//            List<CashFlow> cashFlowList = getCashFlowList(firstDay, period, iteration);
//
//            for (final CashFlow cashFlow : cashFlowList) {
//                List<Tag> categories = cashFlow.getTags();
//                for (Tag tag : categories) {
//                    TagStats tagStats = findTagStats(result, tag);
//
//                    if (cashFlow.getType() == CashFlow.Type.INCOME) {
//                        tagStats.incomeAmount(cashFlow.getAmount());
//                    } else if (cashFlow.getType() == CashFlow.Type.EXPANSE) {
//                        tagStats.expanseAmount(cashFlow.getAmount());
//                    }
//                }
//            }
//
//            return result;
//
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
    }

    private List<TagStats> createTagStatsResults(List<Tag> categories) {
        List<TagStats> result = Lists.newArrayListWithCapacity(categories.size());
        Lists.newArrayListWithCapacity(categories.size());
        for (Tag tag : categories) {
            result.add(new TagStats(tag.getId()));
        }
        return result;
    }

    @Override
    public long countDependentCashFlows(Long tagId) {
        return cashFlowService.countAssignedWithTag(tagId);
    }

    private List<CashFlow> getCashFlowList(Date firstDay, Period period, Integer iteration) {
        checkNotNull(firstDay);
        checkNotNull(period);
        checkNotNull(iteration);

        Interval interval = KorzeniowskiUtils.Times.getInterval(new DateTime(firstDay), period, iteration);
        return cashFlowService.findCashFlow(interval.getStart().toDate(), interval.getEnd().toDate(), null, null);
    }


    private TagStats findTagStats(List<TagStats> list, final Tag tag) {
        return Iterables.find(list, new Predicate<TagStats>() {
            @Override
            public boolean apply(TagStats input) {
                return tag.getId().equals(input.getTagId());
            }
        });
    }
}
