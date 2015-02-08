package info.korzeniowski.walletplus.test.module;

import org.mockito.Mockito;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.UserDatabaseHelper;
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
        complete = false
)
public class MockDatabaseModule {

    public MockDatabaseModule() {
    }

    @Provides
    @Singleton
    public UserDatabaseHelper provideUserDatabaseHelper() {
        return Mockito.mock(UserDatabaseHelper.class);
    }

    @Provides
    @Named("local")
    @Singleton
    public WalletService provideMockWalletService() {
        return Mockito.mock(WalletService.class);
    }
}
