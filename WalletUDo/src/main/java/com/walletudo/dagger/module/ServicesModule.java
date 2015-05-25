package com.walletudo.dagger.module;

import com.j256.ormlite.dao.Dao;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Profile;
import com.walletudo.model.Tag;
import com.walletudo.model.TagAndCashFlowBind;
import com.walletudo.model.Wallet;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.ProfileService;
import com.walletudo.service.StatisticService;
import com.walletudo.service.TagService;
import com.walletudo.service.WalletService;
import com.walletudo.service.ormlite.CashFlowServiceOrmLite;
import com.walletudo.service.ormlite.MainDatabaseHelper;
import com.walletudo.service.ormlite.ProfileDatabaseHelper;
import com.walletudo.service.ormlite.ProfileServiceOrmLite;
import com.walletudo.service.ormlite.StatisticServiceOrmLite;
import com.walletudo.service.ormlite.TagServiceOrmLite;
import com.walletudo.service.ormlite.WalletServiceOrmLite;

import java.sql.SQLException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
    public Dao<Tag, Long> provideTagDao(ProfileDatabaseHelper profileDatabaseHelper) {
        try {
            return profileDatabaseHelper.getTagDao();
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
    public Dao<CashFlow, Long> provideCashFlowDao(ProfileDatabaseHelper profileDatabaseHelper) {
        try {
            return profileDatabaseHelper.getCashFlowDao();
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
    public Dao<TagAndCashFlowBind, Long> provideTagAndCashFlowDao(ProfileDatabaseHelper profileDatabaseHelper) {
        try {
            return profileDatabaseHelper.getTagAndCashFlowBindsDao();
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
    public Dao<Wallet, Long> provideWalletDao(ProfileDatabaseHelper profileDatabaseHelper) {
        try {
            return profileDatabaseHelper.getWalletDao();
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
