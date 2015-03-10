package info.korzeniowski.walletplus.service.ormlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.Strings;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.TagAndCashFlowBind;
import info.korzeniowski.walletplus.model.Wallet;

public class UserDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private Dao<Wallet, Long> walletDao;
    private Dao<Tag, Long> tagDao;
    private Dao<CashFlow, Long> cashFlowDao;
    private Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao;

    private WeakReference<WalletPlus> walletPlus;

    public UserDatabaseHelper(Context context, String profileName) {
        super(context, profileName != null ? profileName + ".db" : null, null, DATABASE_VERSION);
        this.walletPlus = new WeakReference<>((WalletPlus) context);
    }

    public Dao<Wallet, Long> getWalletDao() throws SQLException {
        if (walletDao == null) {
            walletDao = getDao(Wallet.class);
        }
        return walletDao;
    }

    public Dao<Tag, Long> getTagDao() throws SQLException {
        if (tagDao == null) {
            tagDao = getDao(Tag.class);
        }
        return tagDao;
    }

    public Dao<CashFlow, Long> getCashFlowDao() throws SQLException {
        if (cashFlowDao == null) {
            cashFlowDao = getDao(CashFlow.class);
        }
        return cashFlowDao;
    }

    public Dao<TagAndCashFlowBind, Long> getTagAndCashFlowBindsDao() throws SQLException {
        if (tagAndCashFlowBindsDao == null) {
            tagAndCashFlowBindsDao = getDao(TagAndCashFlowBind.class);
        }
        return tagAndCashFlowBindsDao;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Wallet.class);
            TableUtils.createTable(connectionSource, Tag.class);
            TableUtils.createTable(connectionSource, CashFlow.class);
            TableUtils.createTable(connectionSource, TagAndCashFlowBind.class);
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
        tagDao = null;
        cashFlowDao = null;
        walletPlus = null;
    }
}
