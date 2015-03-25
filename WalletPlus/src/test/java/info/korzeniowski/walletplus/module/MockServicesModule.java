package info.korzeniowski.walletplus.module;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.WalletService;

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
