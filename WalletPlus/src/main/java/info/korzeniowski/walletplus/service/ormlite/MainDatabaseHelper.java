package info.korzeniowski.walletplus.service.ormlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.Profile;

public class MainDatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String MAIN_DATABASE_NAME = "WalletPlus.db";

    private static final int DATABASE_VERSION = 1;

    private Dao<Profile, Long> profileDao;

    @Inject
    public MainDatabaseHelper(Context context) {
        super(context, MAIN_DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Dao<Profile, Long> getProfileDao() throws SQLException {
        if (profileDao == null) {
            profileDao = getDao(Profile.class);
        }
        return profileDao;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Profile.class);
        } catch (SQLException e) {
            Log.e(MainDatabaseHelper.class.getName(), "Can't create main database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    @Override
    public void close() {
        profileDao = null;
    }
}
