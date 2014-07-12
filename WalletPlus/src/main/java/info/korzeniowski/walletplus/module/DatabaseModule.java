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
import info.korzeniowski.walletplus.datamanager.AccountDataManager;
import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.local.DatabaseHelper;
import info.korzeniowski.walletplus.datamanager.local.LocalAccountDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCashFlowDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalWalletDataManager;
import info.korzeniowski.walletplus.drawermenu.cashflow.CashFlowFragment_;
import info.korzeniowski.walletplus.drawermenu.cashflow.DetailsOfRegularCashFlowFragment_;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment_;
import info.korzeniowski.walletplus.drawermenu.category.CategoryListFragment_;
import info.korzeniowski.walletplus.drawermenu.dashboard.DashboardFragment_;
import info.korzeniowski.walletplus.drawermenu.wallet.WalletDetailsFragment_;
import info.korzeniowski.walletplus.drawermenu.wallet.WalletListFragment_;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

/**
 * Module for Database objects.
 */
//TODO: usunąć library = true
@Module(
        library = true,
        injects = {
                DashboardFragment_.class,

                CategoryDetailsFragment_.class,
                CategoryListFragment_.class,
                LocalCategoryDataManager.class,

                DetailsOfRegularCashFlowFragment_.class,
                CashFlowFragment_.class,

                WalletDetailsFragment_.class,
                WalletListFragment_.class
        }
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

    /****************
     * ACCOUNT
     ***************/
    @Provides
    public Dao<Account, Long> provideAccountDao() {
        try {
            return databaseHelper.getAccountDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
    public CategoryDataManager provideCategoryDataManager(LocalCategoryDataManager localCategoryDataManager) {
        return localCategoryDataManager;
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
    public CashFlowDataManager provideCashFlowDataManager(LocalCashFlowDataManager localCashFlowDataManager) {
        return localCashFlowDataManager;
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
    public WalletDataManager provideWalletDataManager(LocalWalletDataManager localWalletDataManager) {
        return localWalletDataManager;
    }
}
