package com.walletudo.service.ormlite;

import android.content.Context;

import com.google.common.io.Files;
import com.j256.ormlite.dao.Dao;
import com.walletudo.model.Profile;
import com.walletudo.service.ProfileService;
import com.walletudo.service.exception.DatabaseException;
import com.walletudo.util.PrefUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

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
        } catch (SQLException e) {
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
        List<Profile> allProfiles = getAll();
        if (activeProfile == null && !allProfiles.isEmpty()) {
            activeProfile = allProfiles.get(0);
        }

        return activeProfile;
    }

    public void actualProfileHasChanged() {
        update(getActiveProfile().setSynchronized(false));
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
            profileDao.update(newValue);
            if (!original.getName().equals(newValue.getName())) {
                File oldDatabaseFile = context.get().getDatabasePath(original.getDatabaseFileName());
                File newDatabaseFile = context.get().getDatabasePath(newValue.getDatabaseFileName());
                Files.move(oldDatabaseFile, newDatabaseFile);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Profile profile = profileDao.queryForId(id);
            context.get().getDatabasePath(profile.getDatabaseFileName()).delete();
            profileDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
