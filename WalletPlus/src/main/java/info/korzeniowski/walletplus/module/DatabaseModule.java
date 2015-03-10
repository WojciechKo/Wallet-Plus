package info.korzeniowski.walletplus.module;

import com.j256.ormlite.dao.Dao;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
import info.korzeniowski.walletplus.util.PrefUtils;

/**
 * Module for Database objects.
 */
@Module
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
    public UserDatabaseHelper provideUserDatabaseHelper(ProfileServiceOrmLite profileService, PrefUtils prefUtils) {
        Profile profile = profileService.findById(prefUtils.getActiveProfileId());
        if (profile != null) {
            return new UserDatabaseHelper(application.get(), profile.getName());
        }
        return new UserDatabaseHelper(application.get(), null);
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
    @Singleton
    public StatisticService provideStatisticService(StatisticServiceOrmLite statisticServiceOrmLite) {
        return statisticServiceOrmLite;
    }
}
