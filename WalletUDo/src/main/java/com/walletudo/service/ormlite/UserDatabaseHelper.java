package com.walletudo.service.ormlite;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Tag;
import com.walletudo.model.TagAndCashFlowBind;
import com.walletudo.model.Wallet;
import com.walletudo.util.Utils;

import java.io.File;
import java.sql.SQLException;

public class UserDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private Dao<Wallet, Long> walletDao;
    private Dao<Tag, Long> tagDao;
    private Dao<CashFlow, Long> cashFlowDao;
    private Dao<TagAndCashFlowBind, Long> tagAndCashFlowBindsDao;

    public UserDatabaseHelper(Context context, String profileName) {
        super(new DatabaseContext(context), profileName, null, DATABASE_VERSION);
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
    }

    static class DatabaseContext extends ContextWrapper {

        private static final String DEBUG_CONTEXT = "DatabaseContext";

        public DatabaseContext(Context base) {
            super(base);
        }

        @Override
        public File getDatabasePath(String name) {

            String dbfile = Utils.getMainDatabaseFolder(getApplicationContext()) + name;
            if (!dbfile.endsWith(".db")) {
                dbfile += ".db";
            }

            File result = new File(dbfile);

            if (!result.getParentFile().exists()) {
                result.getParentFile().mkdirs();
            }

            if (Log.isLoggable(DEBUG_CONTEXT, Log.WARN)) {
                Log.w(DEBUG_CONTEXT,
                        "getDatabasePath(" + name + ") = " + result.getAbsolutePath());
            }

            return result;
        }

        /* this version is called for android devices >= api-11. thank to @damccull for fixing this. */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
            return openOrCreateDatabase(name, mode, factory);
        }

        /* this version is called for android devices < api-11 */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
            // SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
            if (Log.isLoggable(DEBUG_CONTEXT, Log.WARN)) {
                Log.w(DEBUG_CONTEXT,
                        "openOrCreateDatabase(" + name + ",,) = " + result.getPath());
            }
            return result;
        }
    }
}
