package info.korzeniowski.walletplus.module;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCashFlowDataManager;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment_;
import info.korzeniowski.walletplus.drawermenu.category.CategoryListFragment_;
import info.korzeniowski.walletplus.drawermenu.cashflow.CashFlowDetailsFragment_;
import info.korzeniowski.walletplus.drawermenu.cashflow.CashFlowListFragment_;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;
import info.korzeniowski.walletplus.model.greendao.GreenCashFlowDao;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;


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
                CashFlowListFragment_.class
        }
)
public class DatabaseModule {
    private static SQLiteOpenHelper dbHelper;
    private static SQLiteDatabase database;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public DatabaseModule(WalletPlus application) {
        dbHelper = new DaoMaster.DevOpenHelper(application, application.DATABASE_NAME, null);
        database = dbHelper.getWritableDatabase();
        daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
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
}
