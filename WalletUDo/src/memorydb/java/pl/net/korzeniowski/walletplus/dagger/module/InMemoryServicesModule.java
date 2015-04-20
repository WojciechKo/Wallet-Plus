package com.walletudo.dagger.module;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.mockito.Mockito;

import java.sql.SQLException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
import com.walletudo.service.ormlite.ProfileServiceOrmLite;
import com.walletudo.service.ormlite.StatisticServiceOrmLite;
import com.walletudo.service.ormlite.TagServiceOrmLite;
import com.walletudo.service.ormlite.WalletServiceOrmLite;
import com.walletudo.util.PrefUtils;

/**
 * Module for Database objects.
 */
@Module
public class InMemoryServicesModule {

    @Provides
    @Singleton
    public Context provideContext() {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.dataDir = "";

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getApplicationInfo()).thenReturn(applicationInfo);
        return context;
    }

    @Provides
    @Singleton
    public PrefUtils providePrefUtils() {
        return Mockito.mock(PrefUtils.class);
    }

    @Provides
    @Singleton
    public ConnectionSource provideConnectionSource() {
        try {
            JdbcConnectionSource connectionSource = new JdbcConnectionSource("jdbc:sqlite::memory:");
            TableUtils.createTable(connectionSource, Wallet.class);
            TableUtils.createTable(connectionSource, Tag.class);
            TableUtils.createTable(connectionSource, CashFlow.class);
            TableUtils.createTable(connectionSource, TagAndCashFlowBind.class);
            TableUtils.createTable(connectionSource, Profile.class);
            return connectionSource;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * *************
     * PROFILE
     * *************
     */
    @Provides
    @Singleton
    public Dao<Profile, Long> provideProfileDao(ConnectionSource connectionSource) {
        try {
            return DaoManager.createDao(connectionSource, Profile.class);
        } catch (SQLException e) {
            return null;
        }
    }

    @Provides
    @Singleton
    public ProfileService provideProfileService(ProfileServiceOrmLite profileServiceOrmLite) {
        profileServiceOrmLite.insert(new Profile().setName("Test profile"));
        return profileServiceOrmLite;
    }

    /**
     * *************
     * TAG
     * *************
     */
    @Provides
    @Singleton
    public Dao<Tag, Long> provideTagDao(ConnectionSource connectionSource) {
        try {
            return DaoManager.createDao(connectionSource, Tag.class);
        } catch (SQLException e) {
            return null;
        }
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
    @Singleton
    public Dao<CashFlow, Long> provideCashFlowDao(ConnectionSource connectionSource) {
        try {
            return DaoManager.createDao(connectionSource, CashFlow.class);
        } catch (SQLException e) {
            return null;
        }
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
    @Singleton
    public Dao<TagAndCashFlowBind, Long> provideTagAndCashFlowDao(ConnectionSource connectionSource) {
        try {
            return DaoManager.createDao(connectionSource, TagAndCashFlowBind.class);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * *************
     * WALLET
     * *************
     */
    @Provides
    @Singleton
    public Dao<Wallet, Long> provideWalletDao(ConnectionSource connectionSource) {
        try {
            return DaoManager.createDao(connectionSource, Wallet.class);
        } catch (SQLException e) {
            return null;
        }
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
