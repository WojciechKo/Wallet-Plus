package info.korzeniowski.walletplus.service.ormlite;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.util.PrefUtils;
import info.korzeniowski.walletplus.util.Utils;

public class ProfileServiceOrmLite implements ProfileService {
    private final Dao<Profile, Long> profileDao;
    private final WeakReference<Context> context;
    private final PrefUtils prefUtils;

    @Inject
    public ProfileServiceOrmLite(Context context, Dao<Profile, Long> profileDao, PrefUtils prefUtils) {
        this.prefUtils = prefUtils;
        this.context = new WeakReference(context);
        this.profileDao = profileDao;
    }

    @Override
    public Long insert(Profile entity) {
        try {
            entity.setDatabaseFilePath(Utils.getProfileDatabaseFilePath(context.get(), entity.getName()));
            profileDao.create(entity);
            return entity.getId();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Long count() {
        try {
            return profileDao.countOf();
        }catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Profile findById(Long id) {
        try {
            return profileDao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Profile findByName(String name) {
        try {
            return profileDao.queryBuilder().where().eq(Profile.NAME_COLUMN_NAME, name).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Profile getActiveProfile() {
        Long activeProfileId = prefUtils.getActiveProfileId();
        Profile activeProfile = null;
        if (activeProfileId != -1) {
            activeProfile = findById(activeProfileId);
        }
        if (activeProfile == null && count() != 0) {
            activeProfile = getAll().get(0);
        }

        return activeProfile;
    }

    @Override
    public List<Profile> getAll() {
        try {
            return profileDao.queryBuilder().orderByRaw(Profile.NAME_COLUMN_NAME + " COLLATE NOCASE").query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void update(Profile newValue) {
        try {
            Profile original = profileDao.queryForId(newValue.getId());
            newValue.setDatabaseFilePath(Utils.getProfileDatabaseFilePath(context.get(), newValue.getName()));
            profileDao.update(newValue);
            if (!original.getName().equals(newValue.getName())) {
                File database = new File(original.getDatabaseFilePath());
                database.renameTo(new File(newValue.getDatabaseFilePath()));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Profile profile = profileDao.queryForId(id);
            deleteDatabaseFile(profile.getDatabaseFilePath());
            profileDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void deleteDatabaseFile(String databaseFileName) {
        throw new RuntimeException("Not implemented!");
    }
}
