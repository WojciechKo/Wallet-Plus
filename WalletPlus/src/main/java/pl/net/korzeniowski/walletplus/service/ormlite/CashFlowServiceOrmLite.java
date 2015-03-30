package pl.net.korzeniowski.walletplus.service.ormlite;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.model.TagAndCashFlowBind;
import pl.net.korzeniowski.walletplus.model.Wallet;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.service.aspect.DatabaseChanger;
import pl.net.korzeniowski.walletplus.service.exception.DatabaseException;
import pl.net.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;

public class CashFlowServiceOrmLite implements CashFlowService {
    private final Dao<CashFlow, Long> cashFlowDao;
    private final Dao<Wallet, Long> walletDao;
    private final Dao<Tag, Long> tagDao;
    private final Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao;
    private final TagServiceOrmLite tagServiceOrmLite;

    private PreparedQuery<Tag> tagsOfCashFlowQuery;

    @Inject
    public CashFlowServiceOrmLite(Dao<CashFlow, Long> cashFlowDao,
                                  Dao<Wallet, Long> walletDao,
                                  Dao<Tag, Long> tagDao,
                                  Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao,
                                  TagServiceOrmLite tagServiceOrmLite) {
        this.cashFlowDao = cashFlowDao;
        this.walletDao = walletDao;
        this.tagDao = tagDao;
        this.tagAndCashFlowBindsDao = tagAndCashFlowBindsDao;
        this.tagServiceOrmLite = tagServiceOrmLite;
    }

    /**
     * CREATE
     */
    @Override
    @DatabaseChanger
    public Long insert(CashFlow cashFlow) {
        try {
            validateInsert(cashFlow);
            cashFlowDao.create(cashFlow);
            bindWithTags(cashFlow);
            fixCurrentAmountInWalletAfterInsert(cashFlow);
            return cashFlow.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * READ
     */
    @Override
    public Long count() {
        try {
            return cashFlowDao.countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public CashFlow findById(final Long id) {
        try {
            CashFlow cashFlow = cashFlowDao.queryForId(id);
            if (cashFlow != null) {
                cashFlow.addTag(getTagsOfCashFlow(cashFlow.getId()));
            }
            return cashFlow;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<CashFlow> getAll() {
        try {
            List<CashFlow> cashFlows = cashFlowDao.queryBuilder().orderBy(CashFlow.DATETIME_COLUMN_NAME, false).query();
            for (CashFlow cashFlow : cashFlows) {
                cashFlow.addTag(getTagsOfCashFlow(cashFlow.getId()));
            }

            return cashFlows;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<CashFlow> findCashFlows(CashFlowQuery query) {
        try {
            QueryBuilder<CashFlow, Long> queryBuilder = cashFlowDao.queryBuilder();
            queryBuilder.setWhere(getWhere(query, queryBuilder));
            List<CashFlow> result = queryBuilder.orderBy(CashFlow.DATETIME_COLUMN_NAME, false).query();
            for (CashFlow cashFlow : result) {
                cashFlow.addTag(getTagsOfCashFlow(cashFlow.getId()));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private Where<CashFlow, Long> getWhere(CashFlowQuery query, QueryBuilder<CashFlow, Long> queryBuilder) throws SQLException {
        Where<CashFlow, Long> where = queryBuilder.where();
        int numClauses = 0;

        if (query.getWalletId() != null) {
            where.eq(CashFlow.WALLET_ID_COLUMN_NAME, query.getWalletId());
            numClauses++;
        }
        if (query.getFromDate() != null) {
            where.ge(CashFlow.DATETIME_COLUMN_NAME, query.getFromDate());
            numClauses++;
        }
        if (query.getToDate() != null) {
            where.le(CashFlow.DATETIME_COLUMN_NAME, query.getToDate());
            numClauses++;
        }
        if (query.getMinAmount() != null) {
            where.ge(CashFlow.AMOUNT_COLUMN_NAME, query.getMinAmount());
            numClauses++;
        }
        if (query.getMaxAmount() != null) {
            where.le(CashFlow.AMOUNT_COLUMN_NAME, query.getMaxAmount());
            numClauses++;
        }
        if (!query.getWithTagSet().isEmpty()) {
            QueryBuilder<TagAndCashFlowBind, Long> tagCashFlowQb = tagAndCashFlowBindsDao.queryBuilder();
            tagCashFlowQb.selectColumns(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME);
            tagCashFlowQb.where().in(TagAndCashFlowBind.TAG_ID_COLUMN_NAME, query.getWithTagSet());
            tagCashFlowQb
                    .groupBy(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME)
                    .having("COUNT(" + TagAndCashFlowBind.TAG_ID_COLUMN_NAME + ") = " + query.getWithTagSet().size());

            where.in(CashFlow.ID_COLUMN_NAME, tagCashFlowQb);
            numClauses++;
        }
        if (!query.getWithoutTagSet().isEmpty()) {
            QueryBuilder<TagAndCashFlowBind, Long> tagCashFlowQb = tagAndCashFlowBindsDao.queryBuilder();
            tagCashFlowQb.selectColumns(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME);
            tagCashFlowQb.where().in(TagAndCashFlowBind.TAG_ID_COLUMN_NAME, query.getWithoutTagSet());

            where.notIn(CashFlow.ID_COLUMN_NAME, tagCashFlowQb);
            numClauses++;
        }

        return where.and(numClauses);
    }

    @Override
    public List<CashFlow> getLastNCashFlows(int n) {
        try {
            List<CashFlow> result = cashFlowDao.queryBuilder().orderBy(CashFlow.DATETIME_COLUMN_NAME, false).limit((long) n).query();
            for (CashFlow cashFlow : result) {
                cashFlow.addTag(getTagsOfCashFlow(cashFlow.getId()));
            }
            return Lists.reverse(result);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private List<Tag> getTagsOfCashFlow(Long cashFlowId) throws SQLException {
        if (tagsOfCashFlowQuery == null) {
            tagsOfCashFlowQuery = getTagsOfCashFlowQuery();
        }
        tagsOfCashFlowQuery.setArgumentHolderValue(0, cashFlowId);
        return tagDao.query(tagsOfCashFlowQuery);
    }

    private PreparedQuery<Tag> getTagsOfCashFlowQuery() throws SQLException {
        QueryBuilder<TagAndCashFlowBind, Long> tagCashFlowQb = tagAndCashFlowBindsDao.queryBuilder();
        tagCashFlowQb.selectColumns(TagAndCashFlowBind.TAG_ID_COLUMN_NAME);
        SelectArg cashFlowIdArg = new SelectArg();
        tagCashFlowQb.where().eq(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME, cashFlowIdArg);

        QueryBuilder<Tag, Long> tagQb = tagDao.queryBuilder();
        tagQb.where().in(Tag.ID_COLUMN_NAME, tagCashFlowQb);
        return tagQb.prepare();
    }

    /**
     * UPDATE
     */
    @Override
    @DatabaseChanger
    public void update(CashFlow cashFlow) {
        try {
            CashFlow toUpdate = findById(cashFlow.getId());
            validateUpdate(toUpdate, cashFlow);
            cashFlowDao.update(cashFlow);
            unbindFromTags(toUpdate);
            bindWithTags(cashFlow);
            fixCurrentAmountInWalletAfterDelete(toUpdate);
            fixCurrentAmountInWalletAfterInsert(cashFlow);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void validateUpdate(CashFlow old, CashFlow newValue) {
        validateInsert(newValue);
    }

    private void validateInsert(CashFlow cashFlow) {
        if (cashFlow.getType() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(cashFlow.getClass().getSimpleName(), CashFlow.TYPE_COLUMN_NAME);
        }

        if (cashFlow.getWallet() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(cashFlow.getClass().getSimpleName(), CashFlow.WALLET_ID_COLUMN_NAME);
        }

        if (cashFlow.getAmount() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(cashFlow.getClass().getSimpleName(), CashFlow.AMOUNT_COLUMN_NAME);
        }

        if (cashFlow.getDateTime() == null) {
            cashFlow.setDateTime(new Date());
        }
    }

    private void bindWithTags(CashFlow cashFlow) throws SQLException {
        for (Tag tag : cashFlow.getTags()) {
            Tag foundTag = tagServiceOrmLite.findByName(tag.getName());

            if (foundTag == null) {
                foundTag = tag;
                tagServiceOrmLite.insert(foundTag);
            } else {
                tag.setId(foundTag.getId());
            }

            tagAndCashFlowBindsDao.create(new TagAndCashFlowBind(foundTag, cashFlow));
        }
    }

    private void fixCurrentAmountInWalletAfterInsert(CashFlow cashFlow) throws SQLException {
        Wallet wallet = walletDao.queryForId(cashFlow.getWallet().getId());

        Double newCurrentAmount = null;
        if (CashFlow.Type.INCOME.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() + cashFlow.getAmount();
        } else if (CashFlow.Type.EXPANSE.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() - cashFlow.getAmount();
        }
        Preconditions.checkNotNull(newCurrentAmount);
        wallet.setCurrentAmount(newCurrentAmount);
        walletDao.update(wallet);
    }

    /**
     * DELETE
     */
    @Override
    @DatabaseChanger
    public void deleteById(Long id) {
        try {
            CashFlow cashFlow = cashFlowDao.queryForId(id);
            cashFlowDao.deleteById(id);
            unbindFromTags(cashFlow);
            fixCurrentAmountInWalletAfterDelete(cashFlow);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void unbindFromTags(CashFlow cashFlow) throws SQLException {
        DeleteBuilder<TagAndCashFlowBind, Long> deleteBuilder = tagAndCashFlowBindsDao.deleteBuilder();

        deleteBuilder.where()
                .eq(TagAndCashFlowBind.CASH_FLOW_ID_COLUMN_NAME, cashFlow)
                .and()
                .in(TagAndCashFlowBind.TAG_ID_COLUMN_NAME, cashFlow.getTags());

        deleteBuilder.delete();
    }

    private void fixCurrentAmountInWalletAfterDelete(CashFlow cashFlow) throws SQLException {
        Wallet wallet = walletDao.queryForId(cashFlow.getWallet().getId());

        Double newCurrentAmount = null;
        if (CashFlow.Type.INCOME.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() - cashFlow.getAmount();
        } else if (CashFlow.Type.EXPANSE.equals(cashFlow.getType())) {
            newCurrentAmount = wallet.getCurrentAmount() + cashFlow.getAmount();
        }
        Preconditions.checkNotNull(newCurrentAmount);
        wallet.setCurrentAmount(newCurrentAmount);
        walletDao.update(wallet);
    }
}
