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
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    private Dao<Wallet, Long> walletDao;
    private Dao<Category, Long> categoryDao;
    private Dao<CashFlow, Long> cashFlowDao;
    private Dao<Account, Long> accountDao;

    @Inject
    public DatabaseHelper(Context context) {
        this(context, "WalletPlus.db");
    }

    public DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
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

    public Dao<Account, Long> getAccountDao() throws SQLException {
        if (accountDao == null) {
            accountDao = getDao(Account.class);
        }
        return accountDao;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Account.class);
            TableUtils.createTable(connectionSource, Wallet.class);
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, CashFlow.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    @Override
    public void close() {
        super.close();
        walletDao = null;
    }
}
