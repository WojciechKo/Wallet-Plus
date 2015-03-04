package info.korzeniowski.walletplus.module;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.test.ui.wallet.details.CreateNewMyWalletTest;
import info.korzeniowski.walletplus.test.ui.wallet.details.EditMyWalletTest;
import info.korzeniowski.walletplus.test.ui.wallet.list.MyWalletListTest;

@Module(
        includes = DatabaseModule.class,
        injects = {
                CreateNewMyWalletTest.class,
                EditMyWalletTest.class,
                MyWalletListTest.class
        },
        overrides = true,
        complete = false,
        library = true
)
public class MockDatabaseModule {

    @Provides
    @Singleton
    public WalletService provideMockWalletService() {
        return Mockito.mock(WalletService.class);
    }

    @Provides
    @Singleton
    public CashFlowService provideMockCashFlowService() {
        return Mockito.mock(CashFlowService.class);
    }

    @Provides
    @Singleton
    public StatisticService provideMockStatisticService() {
        return Mockito.mock(StatisticService.class);
    }
}
