package com.walletudo.service.ormlite;

import com.google.common.collect.Maps;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Tag;
import com.walletudo.model.TagAndCashFlowBind;
import com.walletudo.service.StatisticService;
import com.walletudo.service.exception.DatabaseException;

import org.joda.time.Period;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

public class StatisticServiceOrmLite implements StatisticService {

    private Dao<CashFlow, Long> cashFlowDao;
    private Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindDao;
    private Dao<Tag, Long> tagDao;

    @Inject
    public StatisticServiceOrmLite(
            Dao<CashFlow, Long> cashFlowDao,
            Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindDao,
            Dao<Tag, Long> tagDao) {

        this.cashFlowDao = cashFlowDao;
        this.tagAndCashFlowBindDao = tagAndCashFlowBindDao;
        this.tagDao = tagDao;
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
//            } else if (type == CashFlow.Type.EXPENSE) {
//                stats.expanseAmount(cashFlow.getAmount());
//            }
//        }
//
//        return stats;
    }

    private Long getSelectedTagOnlyStatsQuery(Long tagId, CashFlow.Type type) throws SQLException {

        QueryBuilder<TagAndCashFlowBind, Long> getSelectedTagOnlyStatsSubQuery = tagAndCashFlowBindDao.queryBuilder();
        getSelectedTagOnlyStatsSubQuery.selectRaw("CASE" +
                " WHEN " + TagAndCashFlowBind.TABLE_NAME + "." + TagAndCashFlowBind.TAG_ID_COLUMN_NAME + "=" + tagId +
                " THEN IFNULL(SUM(" + CashFlow.TABLE_NAME + "." + CashFlow.AMOUNT_COLUMN_NAME + "), 0)" +
                " ELSE 0" +
                " END AS sums");
        QueryBuilder<CashFlow, Long> cashFlowQb = cashFlowDao.queryBuilder();
        cashFlowQb.where().eq(CashFlow.TYPE_COLUMN_NAME, type);
        getSelectedTagOnlyStatsSubQuery.join(cashFlowQb);
        getSelectedTagOnlyStatsSubQuery.groupBy(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME);
        getSelectedTagOnlyStatsSubQuery.having("COUNT(" + TagAndCashFlowBind.TAG_ID_COLUMN_NAME + ")=1");

        return tagAndCashFlowBindDao.queryRawValue("SELECT SUM(sums) FROM (" + getSelectedTagOnlyStatsSubQuery.prepareStatementString() + ")");
    }

    @Override
    public Map<Tag, TagStats2> getTagStats2(Tag tag) {
        try {
            QueryBuilder<TagAndCashFlowBind, Long> tagStatisticsQuery = tagAndCashFlowBindDao.queryBuilder();

            tagStatisticsQuery.selectRaw(
                    Tag.TABLE_NAME + "." + Tag.ID_COLUMN_NAME,

                    "CASE" +
                            " WHEN " + Tag.TABLE_NAME + "." + Tag.ID_COLUMN_NAME + "=" + tag.getId() +
                            " THEN " + getSelectedTagOnlyStatsQuery(tag.getId(), CashFlow.Type.INCOME) +
                            " ELSE SUM(CASE" +
                            " WHEN " + CashFlow.TABLE_NAME + "." + CashFlow.TYPE_COLUMN_NAME + "='" + CashFlow.Type.INCOME + "'" +
                            " THEN " + CashFlow.AMOUNT_COLUMN_NAME +
                            " ELSE 0" +
                            " END)" +
                            " END AS " + CashFlow.Type.INCOME,

                    "CASE" +
                            " WHEN " + Tag.TABLE_NAME + "." + Tag.ID_COLUMN_NAME + "=" + tag.getId() +
                            " THEN " + getSelectedTagOnlyStatsQuery(tag.getId(), CashFlow.Type.EXPENSE) +
                            " ELSE SUM(CASE" +
                            " WHEN " + CashFlow.TABLE_NAME + "." + CashFlow.TYPE_COLUMN_NAME + "='" + CashFlow.Type.EXPENSE + "'" +
                            " THEN " + CashFlow.AMOUNT_COLUMN_NAME +
                            " ELSE 0" +
                            " END)" +
                            " END AS " + CashFlow.Type.EXPENSE);


            tagStatisticsQuery.join(cashFlowDao.queryBuilder());
            tagStatisticsQuery.join(tagDao.queryBuilder());

            QueryBuilder<TagAndCashFlowBind, Long> subBindQuery = tagAndCashFlowBindDao.queryBuilder();
            subBindQuery.selectColumns(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME);
            subBindQuery.where().eq(TagAndCashFlowBind.TAG_ID_COLUMN_NAME, tag.getId());
            tagStatisticsQuery.where().in(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME, subBindQuery);

            tagStatisticsQuery.groupByRaw(Tag.TABLE_NAME + "." + Tag.ID_COLUMN_NAME);
            tagStatisticsQuery.orderByRaw(CashFlow.Type.INCOME + " + " + CashFlow.Type.EXPENSE + " DESC");

            Map<Tag, TagStats2> result = Maps.newLinkedHashMap();
            for (String[] resultRow : tagStatisticsQuery.queryRaw()) {
                Tag rowTag = tagDao.queryForId(Long.parseLong(resultRow[0]));
                TagStats2 tagStats = new TagStats2(Double.parseDouble(resultRow[1]), Double.parseDouble(resultRow[2]));
                result.put(rowTag, tagStats);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
