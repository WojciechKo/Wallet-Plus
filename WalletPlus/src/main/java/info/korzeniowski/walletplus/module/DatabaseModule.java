package info.korzeniowski.walletplus.module;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.drawermenu.cashflow.CashFlowDetailsFragment;
import info.korzeniowski.walletplus.drawermenu.cashflow.CashFlowListFragment;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment;
import info.korzeniowski.walletplus.drawermenu.category.CategoryListFragment;
import info.korzeniowski.walletplus.drawermenu.dashboard.DashboardFragment;
import info.korzeniowski.walletplus.drawermenu.wallet.WalletDetailsFragment;
import info.korzeniowski.walletplus.drawermenu.wallet.WalletListFragment;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalCashFlowService;
import info.korzeniowski.walletplus.service.local.LocalCategoryService;
import info.korzeniowski.walletplus.service.local.LocalWalletService;

/**
 * Module for Database objects.
 */
@Module(
        injects = {
                WalletPlus.class,

                DashboardFragment.class,

                CategoryDetailsFragment.class,
                CategoryListFragment.class,

                CashFlowDetailsFragment.class,
                CashFlowListFragment.class,

                WalletDetailsFragment.class,
                WalletListFragment.class
        },
        complete = false
)
public class DatabaseModule {

    private DatabaseHelper databaseHelper;

    public DatabaseModule(WalletPlus application) {
        databaseHelper = getHelper(application);
    }

    private DatabaseHelper getHelper(Context context) {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return databaseHelper;
    }

//    /****************
//     * ACCOUNT
//     ***************/
//    @Provides
//    public Dao<Account, Long> provideAccountDao() {
//        try {
//            return databaseHelper.getAccountDao();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Provides
//    @Named("local")
//    @Singleton
//    public AccountDataManager provideAccountDataManager(LocalAccountDataManager localAccountDataManager) {
//        return localAccountDataManager;
//    }

    /****************
     * CATEGORY
     ***************/
    @Provides
    public Dao<Category, Long> provideCategoryDao() {
        try {
            return databaseHelper.getCategoryDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Named("local")
    @Singleton
    public CategoryService provideCategoryService(LocalCategoryService localCategoryService) {
        return localCategoryService;
    }

    /****************
     * CASH_FLOW
     ***************/
    @Provides
    public Dao<CashFlow, Long> provideCashFlowDao() {
        try {
            return databaseHelper.getCashFlowDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Named("local")
    @Singleton
    public CashFlowService provideCashFlowService(LocalCashFlowService localCashFlowService) {
        return localCashFlowService;
    }

    /****************
     * WALLET
     ***************/
    @Provides
    public Dao<Wallet, Long> provideWalletDao() {
        try {
            return databaseHelper.getWalletDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Named("local")
    @Singleton
    public WalletService provideWalletService(LocalWalletService localWalletService) {
        return localWalletService;
    }
}
