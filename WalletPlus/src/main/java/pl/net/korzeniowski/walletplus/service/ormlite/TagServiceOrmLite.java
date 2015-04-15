package pl.net.korzeniowski.walletplus.service.ormlite;

import android.graphics.Color;

import com.google.common.base.Strings;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.service.ProfileService;
import pl.net.korzeniowski.walletplus.service.TagService;
import pl.net.korzeniowski.walletplus.service.exception.DatabaseException;
import pl.net.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import pl.net.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;

import static com.google.common.base.Preconditions.checkNotNull;

public class TagServiceOrmLite implements TagService {
    private final Dao<Tag, Long> tagDao;
    private ProfileService profileService;

    @Inject
    public TagServiceOrmLite(Dao<Tag, Long> tagDao, ProfileService profileService) {
        this.tagDao = tagDao;
        this.profileService = profileService;
    }

    /**
     * ********
     * CREATE *
     * ********
     */
    @Override
    public Long insert(Tag tag) {
        try {
            validateInsert(tag);
            tagDao.create(tag);
            profileService.actualProfileHasChanged();
            return tag.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void validateInsert(Tag tag) {
        checkNotNull(tag);
        generateColorIfNull(tag);
        validateIfNameIsNotNullOrEmpty(tag);
        validateIfIdIsUnique(tag);
    }

    private void validateIfIdIsUnique(Tag tag) {
        if (tag.getId() != null && findById(tag.getId()) != null) {
            throw new EntityAlreadyExistsException(Tag.class.getSimpleName(), tag.getId());
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
            return tagDao.queryBuilder().where().eq(Tag.NAME_COLUMN_NAME, name).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Tag> getAll() {
        try {
            return tagDao.queryBuilder().orderByRaw(Tag.NAME_COLUMN_NAME + " COLLATE NOCASE").query();
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
            validateUpdate(newValue);
            tagDao.update(newValue);
            profileService.actualProfileHasChanged();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void validateUpdate(Tag newTag) {
        checkNotNull(newTag);
        generateColorIfNull(newTag);
        Tag oldTag = findById(newTag.getId());
        validateIfNameIsNotNullOrEmpty(newTag);
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

    private void validateIfNameIsNotNullOrEmpty(Tag tag) {
        if (Strings.isNullOrEmpty(tag.getName())) {
            throw new EntityPropertyCannotBeNullOrEmptyException(Tag.class.getSimpleName(), Tag.NAME_COLUMN_NAME);
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
            tagDao.deleteById(id);
            profileService.actualProfileHasChanged();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
