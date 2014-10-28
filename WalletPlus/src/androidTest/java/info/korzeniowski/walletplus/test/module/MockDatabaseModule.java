package info.korzeniowski.walletplus.test.module;

import org.mockito.Mockito;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.test.service.cashflow.LocalCashFlowServiceTest;
import info.korzeniowski.walletplus.test.ui.wallet.details.AddingMyWalletFragmentTest;
import info.korzeniowski.walletplus.test.ui.wallet.details.EditingMyWalletFragmentTest;

@Module(
        addsTo = DatabaseModule.class,
        injects = {
                AddingMyWalletFragmentTest.class,
                EditingMyWalletFragmentTest.class,
                LocalCashFlowServiceTest.class
        },
        overrides = true,
        complete = false,
        library = true
)
public class MockDatabaseModule {

    public MockDatabaseModule() {
    }

    @Provides
    @Singleton
    public DatabaseHelper provideMockDatabaseHelper() {
        return Mockito.mock(DatabaseHelper.class);
    }

    @Provides
    @Named("local")
    @Singleton
    public WalletService provideMockWalletService() {
        return Mockito.mock(WalletService.class);
    }
}
