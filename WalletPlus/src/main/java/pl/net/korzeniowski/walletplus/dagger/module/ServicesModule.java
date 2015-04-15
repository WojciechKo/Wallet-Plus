package pl.net.korzeniowski.walletplus.dagger.module;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Profile;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.model.TagAndCashFlowBind;
import pl.net.korzeniowski.walletplus.model.Wallet;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.service.ProfileService;
import pl.net.korzeniowski.walletplus.service.StatisticService;
import pl.net.korzeniowski.walletplus.service.TagService;
import pl.net.korzeniowski.walletplus.service.WalletService;
import pl.net.korzeniowski.walletplus.service.ormlite.CashFlowServiceOrmLite;
import pl.net.korzeniowski.walletplus.service.ormlite.MainDatabaseHelper;
import pl.net.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import pl.net.korzeniowski.walletplus.service.ormlite.StatisticServiceOrmLite;
import pl.net.korzeniowski.walletplus.service.ormlite.TagServiceOrmLite;
import pl.net.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import pl.net.korzeniowski.walletplus.service.ormlite.WalletServiceOrmLite;

/**
 * Module for Database objects.
 */
@Module
public class ServicesModule {
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
