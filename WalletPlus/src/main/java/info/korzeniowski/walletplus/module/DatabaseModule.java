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
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.TagAndCashFlowBind;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.AccountService;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.ormlite.AccountServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.CashFlowServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.StatisticServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.TagServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.WalletServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.MainDatabaseHelper;
import info.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsActivity;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListActivity;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListFragment;
import info.korzeniowski.walletplus.ui.statistics.details.StaticticDetailsActivity;
import info.korzeniowski.walletplus.ui.statistics.details.StatisticDetailsFragment;
import info.korzeniowski.walletplus.ui.statistics.list.StatisticListActivity;
import info.korzeniowski.walletplus.ui.statistics.list.StatisticListFragment;
import info.korzeniowski.walletplus.ui.dashboard.DashboardActivity;
import info.korzeniowski.walletplus.ui.dashboard.DashboardFragment;
import info.korzeniowski.walletplus.ui.tag.details.TagDetailsActivity;
import info.korzeniowski.walletplus.ui.tag.details.TagDetailsFragment;
import info.korzeniowski.walletplus.ui.tag.list.TagListFragment;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsFragment;
import info.korzeniowski.walletplus.ui.wallets.list.WalletListActivity;
import info.korzeniowski.walletplus.ui.wallets.list.WalletListFragment;
import info.korzeniowski.walletplus.ui.tag.list.TagListActivity;
import info.korzeniowski.walletplus.ui.profile.ProfileActivity;
import info.korzeniowski.walletplus.ui.synchronize.SynchronizeActivity;
import info.korzeniowski.walletplus.util.PrefUtils;

/**
 * Module for Database objects.
 */
@Module(
        injects = {
                BaseActivity.class,

                MainActivity.class,

                DashboardActivity.class,
                DashboardFragment.class,

                StatisticListActivity.class,
                StatisticListFragment.class,

                StaticticDetailsActivity.class,
                StatisticDetailsFragment.class,

                CashFlowListActivity.class,
                CashFlowListFragment.class,

                CashFlowDetailsActivity.class,
                CashFlowDetailsFragment.class,

                WalletListActivity.class,
                WalletListFragment.class,

                WalletDetailsActivity.class,
                WalletDetailsFragment.class,

                TagListActivity.class,
                TagListFragment.class,

                TagDetailsActivity.class,
                TagDetailsFragment.class,

                SynchronizeActivity.class,
                SynchronizeActivity.SynchronizeFragment.class,

                ProfileActivity.class,
                ProfileActivity.CreateProfileFragment.class,

                DatabaseInitializer.class,
                AccountServiceOrmLite.class,
                ProfileServiceOrmLite.class
        },
        complete = false,
        library = true
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
    public UserDatabaseHelper provideUserDatabaseHelper(ProfileServiceOrmLite profileService) {
        Profile profile = profileService.findById(PrefUtils.getActiveProfileId(application.get()));
        return new UserDatabaseHelper(application.get(), profile.getName());
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
    @Named(AccountService.ORMLITE_IMPL)
    @Singleton
    public AccountService provideAccountService(AccountServiceOrmLite accountServiceOrmLite) {
        return accountServiceOrmLite;
    }

    /**
     * *************
     * PROFILE
     * *************
     */
    @Provides
    public Dao<Profile, Long> provideProfileDao(MainDatabaseHelper mainDatabaseHelper) {
        try {
            return mainDatabaseHelper.getProfileDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Named(ProfileService.ORMLITE_IMPL)
    @Singleton
    public ProfileService provideProfileService(ProfileServiceOrmLite profileServiceOrmLite) {
        return profileServiceOrmLite;
    }


    /**
     * *************
     * TAG
     * *************
     */
    @Provides
    public Dao<Tag, Long> provideTagDao(UserDatabaseHelper userDatabaseHelper) {
        try {
            return userDatabaseHelper.getTagDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Named(TagService.ORMLITE_IMPL)
    @Singleton
    public TagService provideTagService(TagServiceOrmLite tagServiceOrmLite) {
        return tagServiceOrmLite;
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
    @Named(CashFlowService.ORMLITE_IMPL)
    @Singleton
    public CashFlowService provideCashFlowService(CashFlowServiceOrmLite cashFlowServiceOrmLite) {
        return cashFlowServiceOrmLite;
    }

    /**
     * *************
     * TAG AND CASH_FLOW BIND
     * *************
     */
    @Provides
    public Dao<TagAndCashFlowBind, Long> provideTagAndCashFlowDao(UserDatabaseHelper userDatabaseHelper) {
        try {
            return userDatabaseHelper.getTagAndCashFlowBindsDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
    @Named(WalletService.ORMLITE_IMPL)
    @Singleton
    public WalletService provideWalletService(WalletServiceOrmLite localWalletService) {
        return localWalletService;
    }

    /**
     * *************
     * STATISTIC
     * *************
     */

    @Provides
    @Named(StatisticService.ORMLITE_IMPL)
    @Singleton
    public StatisticService provideStatisticService(StatisticServiceOrmLite statisticServiceOrmLite) {
        return statisticServiceOrmLite;
    }
}
