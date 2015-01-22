package info.korzeniowski.walletplus.module;

import com.j256.ormlite.dao.Dao;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.DatabaseInitializer;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.AccountService;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.LocalAccountService;
import info.korzeniowski.walletplus.service.local.LocalCashFlowService;
import info.korzeniowski.walletplus.service.local.LocalCategoryService;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.service.local.MainDatabaseHelper;
import info.korzeniowski.walletplus.service.local.UserDatabaseHelper;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsActivity;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListActivity;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListFragment;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsActivity;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;
import info.korzeniowski.walletplus.ui.category.list.CategoryListActivity;
import info.korzeniowski.walletplus.ui.category.list.CategoryListFragment;
import info.korzeniowski.walletplus.ui.dashboard.DashboardActivity;
import info.korzeniowski.walletplus.ui.dashboard.DashboardFragment;
import info.korzeniowski.walletplus.ui.mywallets.details.MyWalletDetailsActivity;
import info.korzeniowski.walletplus.ui.mywallets.details.MyWalletDetailsFragment;
import info.korzeniowski.walletplus.ui.mywallets.list.MyWalletListActivity;
import info.korzeniowski.walletplus.ui.mywallets.list.MyWalletListFragment;
import info.korzeniowski.walletplus.ui.otherwallets.details.OtherWalletDetailsActivity;
import info.korzeniowski.walletplus.ui.otherwallets.details.OtherWalletDetailsFragment;
import info.korzeniowski.walletplus.ui.otherwallets.list.OtherWalletListActivity;
import info.korzeniowski.walletplus.ui.otherwallets.list.OtherWalletListFragment;

/**
 * Module for Database objects.
 */
@Module(
        injects = {
                BaseActivity.class,

                MainActivity.class,

                DashboardActivity.class,
                DashboardFragment.class,

                CategoryListActivity.class,
                CategoryListFragment.class,

                CategoryDetailsActivity.class,
                CategoryDetailsFragment.class,

                CashFlowListActivity.class,
                CashFlowListFragment.class,

                CashFlowDetailsActivity.class,
                CashFlowDetailsFragment.class,

                MyWalletListActivity.class,
                MyWalletListFragment.class,

                MyWalletDetailsActivity.class,
                MyWalletDetailsFragment.class,

                OtherWalletListActivity.class,
                OtherWalletListFragment.class,

                OtherWalletDetailsActivity.class,
                OtherWalletDetailsFragment.class,

                DatabaseInitializer.class,
                LocalAccountService.class
        },
        complete = false
)
public class DatabaseModule {

    private final WeakReference<WalletPlus> application;

    public DatabaseModule(WalletPlus application) {
        this.application = new WeakReference<>(application);
    }

    @Provides
    @Singleton
    public MainDatabaseHelper provideMainDatabaseHelper() {
        return new MainDatabaseHelper(application.get());
    }

    @Provides
    @Singleton
    public UserDatabaseHelper provideDatabaseHelper() {
        return new UserDatabaseHelper(application.get());
    }

    /**
     * *************
     * ACCOUNT
     * *************
     */
    @Provides
    public Dao<Account, Long> provideAccountDao(MainDatabaseHelper mainDatabaseHelper) {
        try {
            return mainDatabaseHelper.getAccountDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Named("local")
    @Singleton
    public AccountService provideAccountService(LocalAccountService localAccountService) {
        return localAccountService;
    }

    /**
     * *************
     * CATEGORY
     * *************
     */
    @Provides
    public Dao<Category, Long> provideCategoryDao(UserDatabaseHelper userDatabaseHelper) {
        try {
            return userDatabaseHelper.getCategoryDao();
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
    public Dao<CashFlow, Long> provideCashFlowDao(UserDatabaseHelper userDatabaseHelper) {
        try {
            return userDatabaseHelper.getCashFlowDao();
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
    public Dao<Wallet, Long> provideWalletDao(UserDatabaseHelper userDatabaseHelper) {
        try {
            return userDatabaseHelper.getWalletDao();
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
