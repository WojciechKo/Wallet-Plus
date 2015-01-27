package info.korzeniowski.walletplus.service.local;

import com.j256.ormlite.dao.Dao;

import org.apache.http.impl.cookie.DateParseException;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;

public class LocalProfileService implements ProfileService {
    private final Dao<Profile, Long> profileDao;

    @Inject
    public LocalProfileService(Dao<Profile, Long> profileDao) {
        this. profileDao = profileDao;
    }

    @Override
    public Long insert(Profile entity) {
        try {
            entity.setDatabaseFileName(entity.getName() + ".db");
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
            return profileDao.queryBuilder().where().eq("name", name).queryForFirst();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Profile> getAll() {
        try {
            return profileDao.queryBuilder().orderByRaw("name COLLATE NOCASE").query();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void update(Profile newValue) {
        try {
            profileDao.update(newValue);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Profile profile = profileDao.queryForId(id);
            deleteDatabaseFile(profile.getDatabaseFileName());
            profileDao.deleteById(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void deleteDatabaseFile(String databaseFileName) {
        throw new RuntimeException("Not implemented!");
    }
}
