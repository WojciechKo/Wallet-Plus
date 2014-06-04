package info.korzeniowski.walletplus.module;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.AccountDataManager;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalAccountDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCashFlowDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalWalletDataManager;
import info.korzeniowski.walletplus.datamanager.local.validation.CategoryValidator;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment_;
import info.korzeniowski.walletplus.drawermenu.category.CategoryListFragment_;
import info.korzeniowski.walletplus.drawermenu.cashflow.CashFlowDetailsFragment_;
import info.korzeniowski.walletplus.drawermenu.cashflow.CashFlowListFragment_;
import info.korzeniowski.walletplus.drawermenu.wallet.WalletFragment_;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;
import info.korzeniowski.walletplus.model.greendao.GreenAccountDao;
import info.korzeniowski.walletplus.model.greendao.GreenCashFlowDao;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;
import info.korzeniowski.walletplus.model.greendao.GreenWalletDao;


/**
 * Module for Database objects.
 */
//TODO: usunąć library = true
@Module(
        library = true,
        injects = {
                CategoryDetailsFragment_.class,
                CategoryListFragment_.class,
                LocalCategoryDataManager.class,

                CashFlowDetailsFragment_.class,
                CashFlowListFragment_.class,

                WalletFragment_.class
        }
)
public class DatabaseModule {
    private final DaoSession daoSession;

    public DatabaseModule(WalletPlus application) {
        SQLiteOpenHelper dbHelper = new DaoMaster.DevOpenHelper(application, WalletPlus.DATABASE_NAME, null);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    /****************
     * ACCOUNT
     ***************/
    @Provides
    @Singleton
    public GreenAccountDao provideGreenAccountDao() {
        return daoSession.getGreenAccountDao();
    }

    @Provides
    @Named("local")
    @Singleton
    public AccountDataManager provideAccountDataManager(LocalAccountDataManager localAccountDataManager) {
        return localAccountDataManager;
    }

    /****************
     * CATEGORY
     ***************/
    @Provides
    @Singleton
    public GreenCategoryDao provideGreenCategoryDao() {
        return daoSession.getGreenCategoryDao();
    }

    @Provides
    @Named("local")
    @Singleton
    public CategoryDataManager provideCategoryDataManager(LocalCategoryDataManager localCategoryDataManager) {
        return localCategoryDataManager;
    }

    /****************
     * CASH_FLOW
     ***************/
    @Provides
    @Singleton
    public GreenCashFlowDao provideGreenCashFlowDao() {
        return daoSession.getGreenCashFlowDao();
    }

    @Provides
    @Named("local")
    @Singleton
    public CashFlowDataManager provideCashFlowDataManager(LocalCashFlowDataManager localCashFlowDataManager) {
        return localCashFlowDataManager;
    }

    /****************
     * WALLET
     ***************/
    @Provides
    @Singleton
    public GreenWalletDao provideGreenWalletDao() {
        return daoSession.getGreenWalletDao();
    }

    @Provides
    @Named("local")
    @Singleton
    public WalletDataManager provideWalletDataManager(LocalWalletDataManager localWalletDataManager) {
        return localWalletDataManager;
    }
}
