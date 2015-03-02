package info.korzeniowski.walletplus.service.local;

import android.graphics.Color;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.TagAndCashFlowBind;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.validation.TagValidator;

public class LocalTagService implements TagService {
    private final TagValidator tagValidator;
    private final Dao<Tag, Long> tagDao;
    private final Dao<CashFlow, Long> cashFlowDao;
    private final Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao;

    @Inject
    public LocalTagService(Dao<Tag, Long> tagDao,
                           Dao<CashFlow, Long> cashFlowDao,
                           Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao) {

        this.tagDao = tagDao;
        this.cashFlowDao = cashFlowDao;
        this.tagAndCashFlowBindsDao = tagAndCashFlowBindsDao;
        this.tagValidator = new TagValidator(this);
    }

    public LocalTagService(Dao<Tag, Long> tagDao,
                           Dao<CashFlow, Long> cashFlowDao,
                           Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao,
                           TagValidator tagValidator) {

        this.tagDao = tagDao;
        this.cashFlowDao = cashFlowDao;
        this.tagAndCashFlowBindsDao = tagAndCashFlowBindsDao;
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
            generateColorIfNull(tag);
            tagValidator.validateInsert(tag);
            tagDao.create(tag);
            return tag.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void generateColorIfNull(Tag tag) {
        if (tag.getColor() == null) {
            float[] hsv = new float[3];
            hsv[0] = new Random().nextFloat() * 360; // Hue (0 .. 360)
            hsv[1] = (float) 0.5; // Saturation (0 .. 1)
            hsv[2] = (float) 0.95; // Value (0 .. 1)
            tag.setColor(Color.HSVToColor(hsv));
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
            generateColorIfNull(newValue);
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
     * *******
     * OTHER *
     * *******
     */
    @Override
    public List<CashFlow> getAssociatedCashFlows(Long tagId, Long n) {
        try {
            QueryBuilder<TagAndCashFlowBind, Long> tagCashFlowQb = tagAndCashFlowBindsDao.queryBuilder();
            tagCashFlowQb.selectColumns(TagAndCashFlowBind.CASH_FLOW_ID_FIELD_NAME);
            tagCashFlowQb.where().eq(TagAndCashFlowBind.TAG_ID_FIELD_NAME, tagId);
            tagCashFlowQb.setCountOf(true);

            QueryBuilder<CashFlow, Long> cashFlowQb = cashFlowDao.queryBuilder();
            cashFlowQb.join(tagCashFlowQb);
            cashFlowQb.orderBy(CashFlow.DATETIME_FIELD_NAME, true);
            cashFlowQb.limit(n);
            Long offset = tagCashFlowQb.countOf() - n;
            cashFlowQb.offset(offset);
            return cashFlowQb.query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long countDependentCashFlows(Long tagId) {
        throw new RuntimeException("Not implemented");
    }
}
