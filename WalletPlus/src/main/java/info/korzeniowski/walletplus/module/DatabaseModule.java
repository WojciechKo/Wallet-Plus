package info.korzeniowski.walletplus.module;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment_;
import info.korzeniowski.walletplus.drawermenu.category.CategoryListFragment_;
import info.korzeniowski.walletplus.model.greendao.CategoryGDao;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;


/**
 * Module for Database objects.
 */
@Module(
        injects = {
                CategoryDetailsFragment_.class,
                CategoryListFragment_.class,
                LocalCategoryDataManager.class
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

    @Provides
    @Singleton
    public CategoryGDao provideCategoryDao() {
        return daoSession.getCategoryGDao();
    }

    @Provides
    @Singleton
    public CategoryDataManager provideCategoryDataManager(LocalCategoryDataManager categoryDataManager) {
        return categoryDataManager;
    }
}
