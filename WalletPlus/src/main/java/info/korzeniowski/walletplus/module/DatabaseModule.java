package info.korzeniowski.walletplus.module;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.DatabaseInitializer;
import info.korzeniowski.walletplus.WalletPlus;
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
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowExpanseDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowIncomeDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowBaseDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowTransferDetailsFragment;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;
import info.korzeniowski.walletplus.ui.category.list.CategoryListFragment;
import info.korzeniowski.walletplus.ui.dashboard.DashboardFragment;
import info.korzeniowski.walletplus.ui.wallet.details.WalletDetailsFragment;
import info.korzeniowski.walletplus.ui.wallet.list.WalletListFragment;

/**
 * Module for Database objects.
 */
@Module(
        injects = {
                DatabaseInitializer.class,

                DashboardFragment.class,

                CategoryDetailsFragment.class,
                CategoryListFragment.class,

                CashFlowBaseDetailsFragment.class,
                CashFlowIncomeDetailsFragment.class,
                CashFlowTransferDetailsFragment.class,
                CashFlowExpanseDetailsFragment.class,
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

    /**
     * *************
     * CATEGORY
     * *************
     */
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

    /**
     * *************
     * CASH_FLOW
     * *************
     */
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

    /**
     * *************
     * WALLET
     * *************
     */
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
