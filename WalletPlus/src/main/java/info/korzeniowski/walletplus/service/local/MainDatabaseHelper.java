package info.korzeniowski.walletplus.service.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.Profile;

public class MainDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private Dao<Account, Long> accountDao;
    private Dao<Profile, Long> profileDao;

    @Inject
    public MainDatabaseHelper(Context context) {
        super(context, "WalletPlus.db", null, DATABASE_VERSION);
    }

    public Dao<Account, Long> getAccountDao() throws SQLException {
        if (accountDao == null) {
            accountDao = getDao(Account.class);
        }
        return accountDao;
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
            TableUtils.createTable(connectionSource, Account.class);
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
        accountDao = null;
    }
}
