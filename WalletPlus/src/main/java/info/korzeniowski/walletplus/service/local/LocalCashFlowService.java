package info.korzeniowski.walletplus.service.local;

import com.google.common.base.Preconditions;
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

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.TagAndCashFlowBind;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;

public class LocalCashFlowService implements CashFlowService {
    private final Dao<CashFlow, Long> cashFlowDao;
    private final Dao<Wallet, Long> walletDao;
    private final Dao<Tag, Long> tagDao;
    private final Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao;
    private final LocalTagService localTagService;

    private PreparedQuery<Tag> tagsOfCashFlowQuery;

    @Inject
    public LocalCashFlowService(Dao<CashFlow, Long> cashFlowDao,
                                Dao<Wallet, Long> walletDao,
                                Dao<Tag, Long> tagDao,
                                Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao,
                                LocalTagService localTagService) {
        this.cashFlowDao = cashFlowDao;
        this.walletDao = walletDao;
        this.tagDao = tagDao;
        this.tagAndCashFlowBindsDao = tagAndCashFlowBindsDao;
        this.localTagService = localTagService;
    }

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
            cashFlow.addTag(getTagsOfCashFlow(cashFlow.getId()));
            return cashFlow;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<CashFlow> getAll() {
        try {
            List<CashFlow> cashFlows = cashFlowDao.queryBuilder().orderBy("dateTime", false).query();
            for (CashFlow cashFlow : cashFlows) {
                cashFlow.addTag(getTagsOfCashFlow(cashFlow.getId()));
            }

            return cashFlows;
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
        tagCashFlowQb.selectColumns(TagAndCashFlowBind.TAG_ID_FIELD_NAME);
        SelectArg cashFlowIdArg = new SelectArg();
        tagCashFlowQb.where().eq(TagAndCashFlowBind.CASH_FLOW_ID_FIELD_NAME, cashFlowIdArg);

        QueryBuilder<Tag, Long> tagQb = tagDao.queryBuilder();
        tagQb.where().in(Tag.ID_FIELD_NAME, tagCashFlowQb);
        return tagQb.prepare();
    }

    @Override
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

    }

    @Override
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

    private void validateInsert(CashFlow cashFlow) {
        if (cashFlow.getType() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(cashFlow.getClass().getSimpleName(), "Type");
        }

        if (cashFlow.getWallet() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(cashFlow.getClass().getSimpleName(), "Wallet");
        }
    }

    private void bindWithTags(CashFlow cashFlow) throws SQLException {
        for (Tag tag : cashFlow.getTags()) {
            Tag foundTag = localTagService.findByName(tag.getName());

            if (foundTag == null) {
                foundTag = tag;
                localTagService.insert(foundTag);
            } else {
                tag.setId(foundTag.getId());
            }

            tagAndCashFlowBindsDao.create(new TagAndCashFlowBind(foundTag, cashFlow));
        }
    }

    private void fixCurrentAmountInWalletAfterInsert(CashFlow cashFlow) throws SQLException {
        Wallet wallet = cashFlow.getWallet();

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

    @Override
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
                .eq(TagAndCashFlowBind.CASH_FLOW_ID_FIELD_NAME, cashFlow)
                .and()
                .in(TagAndCashFlowBind.TAG_ID_FIELD_NAME, cashFlow.getTags());

        deleteBuilder.delete();
    }

    private void fixCurrentAmountInWalletAfterDelete(CashFlow cashFlow) throws SQLException {
        Wallet wallet = cashFlow.getWallet();

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

    @Override
    public long countAssignedWithWallet(Long walletId) {
        try {
            return cashFlowDao.queryBuilder().where().eq("wallet_id", walletId).countOf();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long countAssignedWithTag(Long tagId) {
//        try {
            throw new RuntimeException("Not implemented");
//            return cashFlowDao.queryBuilder().where().eq("tag_id", tagId).countOf();
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
    }

    @Override
    public List<CashFlow> findCashFlow(Date from, Date to, Long tagId, Long walletId) {
        try {
            QueryBuilder<CashFlow, Long> queryBuilder = cashFlowDao.queryBuilder();
            queryBuilder.setWhere(getWhereList(from, to, tagId, walletId, queryBuilder));
            return queryBuilder.query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private Where<CashFlow, Long> getWhereList(Date from, Date to, Long tagId, Long walletId, QueryBuilder<CashFlow, Long> queryBuilder) throws SQLException {
        Where<CashFlow, Long> where = queryBuilder.where();
        boolean isFirst = true;

        if (from != null) {
            where.ge("dateTime", from);
            isFirst = false;
        }
        if (to != null) {
            if (!isFirst) {
                where.and();
            }
            where.lt("dateTime", to);
            isFirst = false;
        }
        if (walletId != null) {
            if (!isFirst) {
                where.and();
            }
            if (walletId == WalletService.WALLET_NULL_ID) {
                where.isNull("wallet_id");
            } else {
                where.eq("wallet_id", walletId);
            }
        }

        return where;
    }

    @Override
    public List<CashFlow> getLastNCashFlows(int n) {
        try {
            return cashFlowDao.queryBuilder().orderBy("dateTime", false).limit((long) n).query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
