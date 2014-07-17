package info.korzeniowski.walletplus.datamanager.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Date;

import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "WalletPlus.db";
    public static final int DATABASE_VERSION = 4;
    private Dao<Wallet, Long> walletDao;
    private Dao<Category, Long> categoryDao;
    private Dao<CashFlow, Long> cashFlowDao;
    private Dao<Account, Long> accountDao;

    public DatabaseHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
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

            insertInitialData();
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.clearTable(connectionSource, CashFlow.class);
            TableUtils.clearTable(connectionSource, Category.class);
            TableUtils.clearTable(connectionSource, Wallet.class);
            TableUtils.clearTable(connectionSource, Account.class);

            insertInitialData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
        walletDao = null;
    }

    public void insertInitialData() throws SQLException{
        Dao<Wallet, Long> walletDao = getWalletDao();
        Wallet portfel = new Wallet().setType(Wallet.Type.MY_WALLET).setName("Portfel").setInitialAmount(100d).setCurrentAmount(100d);
        walletDao.create(portfel);
        Wallet skarpeta = new Wallet().setType(Wallet.Type.MY_WALLET).setName("Skarpeta").setInitialAmount(1000d).setCurrentAmount(1000d);
        walletDao.create(skarpeta);

        Wallet biedronka = new Wallet().setType(Wallet.Type.CONTRACTOR).setName("Biedronka").setInitialAmount(0d).setCurrentAmount(0d);
        walletDao.create(biedronka);
        Wallet tesco = new Wallet().setType(Wallet.Type.CONTRACTOR).setName("Tesco").setInitialAmount(0d).setCurrentAmount(0d);
        walletDao.create(tesco);
        Wallet liderPrice = new Wallet().setType(Wallet.Type.CONTRACTOR).setName("Lider Price").setInitialAmount(0d).setCurrentAmount(0d);
        walletDao.create(liderPrice);

        Dao<Category, Long> categoryDao = getCategoryDao();
        Category mainDom = new Category().setName("Dom").setType(Category.Type.EXPENSE);
        categoryDao.create(mainDom);
        categoryDao.create(new Category().setParent(mainDom).setName("Prąd"));
        categoryDao.create(new Category().setParent(mainDom).setName("Woda"));
        categoryDao.create(new Category().setParent(mainDom).setName("Gaz"));

        Category mainInternet = new Category().setName("Internet").setType(Category.Type.INCOME);
        categoryDao.create(mainInternet);
        categoryDao.create(new Category().setParent(mainInternet).setName("Hokus Pokus www"));
        categoryDao.create(new Category().setParent(mainInternet).setName("Inform http"));

        Category main3 = new Category().setName("Żona").setType(Category.Type.INCOME_EXPENSE);
        categoryDao.create(main3);

        Dao<CashFlow, Long> cashFlowDao = getCashFlowDao();
        cashFlowDao.create(new CashFlow(11f, new Date()).setCategory(mainDom).setFromWallet(portfel).setComment("Środki czystości"));
        cashFlowDao.create(new CashFlow(22f, new Date()).setCategory(mainDom).setFromWallet(portfel));
        categoryDao.refresh(mainDom);
        cashFlowDao.create(new CashFlow(33f, new Date()).setCategory(mainDom.getChildren().iterator().next()).setFromWallet(portfel));

        cashFlowDao.create(new CashFlow(44f, new Date()).setCategory(mainInternet).setToWallet(skarpeta).setComment("AdSense"));
        cashFlowDao.create(new CashFlow(55f, new Date()).setCategory(mainInternet).setToWallet(portfel));

        cashFlowDao.create(new CashFlow(66f, new Date()).setFromWallet(portfel).setToWallet(skarpeta));
        cashFlowDao.create(new CashFlow(77f, new Date()).setFromWallet(portfel).setToWallet(biedronka));
    }
}
