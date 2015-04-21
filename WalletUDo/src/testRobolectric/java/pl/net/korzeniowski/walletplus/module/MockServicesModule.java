package com.walletudo.dagger;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.ProfileService;
import com.walletudo.service.StatisticService;
import com.walletudo.service.TagService;
import com.walletudo.service.WalletService;

@Module
public class MockServicesModule {

    @Provides
    @Singleton
    public ProfileService provideProfileService() {
        return Mockito.mock(ProfileService.class);
    }

    @Provides
    @Singleton
    public TagService provideTagService() {
        return Mockito.mock(TagService.class);
    }

    @Provides
    @Singleton
    public CashFlowService provideCashFlowService() {
        return Mockito.mock(CashFlowService.class);

    }

    @Provides
    @Singleton
    public WalletService provideWalletService() {
        return Mockito.mock(WalletService.class);
    }

    @Provides
    @Singleton
    public StatisticService provideStatisticService() {
        return Mockito.mock(StatisticService.class);
    }
}
