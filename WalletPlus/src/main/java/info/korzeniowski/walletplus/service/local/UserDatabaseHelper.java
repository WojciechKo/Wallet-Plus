package info.korzeniowski.walletplus.service.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

public class UserDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private Dao<Wallet, Long> walletDao;
    private Dao<Category, Long> categoryDao;
    private Dao<CashFlow, Long> cashFlowDao;
    private WeakReference<WalletPlus> walletPlus;

    public UserDatabaseHelper(Context context, String profileName) {
        super(context, profileName + ".db", null, DATABASE_VERSION);
        this.walletPlus = new WeakReference<>((WalletPlus) context);
    }

    public Dao<Wallet, Long> getWalletDao() throws SQLException {
        if (walletDao == null) {
            walletDao = getDao(Wallet.class);
        }
        return walletDao;
    }

    public Dao<Category, Long> getCategoryDao() throws SQLException {
        if (categoryDao == null) {
            categoryDao = getDao(Category.class);
        }
        return categoryDao;
    }

    public Dao<CashFlow, Long> getCashFlowDao() throws SQLException {
        if (cashFlowDao == null) {
            cashFlowDao = getDao(CashFlow.class);
        }
        return cashFlowDao;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Wallet.class);
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, CashFlow.class);
        } catch (SQLException e) {
            Log.e(UserDatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    }

    @Override
    public void close() {
        walletDao = null;
        categoryDao = null;
        cashFlowDao = null;
        walletPlus = null;
    }
}
