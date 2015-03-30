package pl.net.korzeniowski.walletplus.module;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.service.ProfileService;
import pl.net.korzeniowski.walletplus.service.StatisticService;
import pl.net.korzeniowski.walletplus.service.TagService;
import pl.net.korzeniowski.walletplus.service.WalletService;

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
