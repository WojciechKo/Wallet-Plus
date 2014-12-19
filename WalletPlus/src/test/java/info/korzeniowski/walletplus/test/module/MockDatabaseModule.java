package info.korzeniowski.walletplus.test.module;

import com.google.common.collect.Lists;

import org.mockito.Mockito;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.UserDatabaseHelper;
import info.korzeniowski.walletplus.test.ui.wallet.details.AddingMyWalletFragmentTest;
import info.korzeniowski.walletplus.test.ui.wallet.details.EditingMyWalletFragmentTest;
import info.korzeniowski.walletplus.test.ui.wallet.list.WalletListFragmentTest;

@Module(
        includes = DatabaseModule.class,
        injects = {
                AddingMyWalletFragmentTest.class,
                EditingMyWalletFragmentTest.class,
                WalletListFragmentTest.class
        },
        overrides = true,
        complete = false
)
public class MockDatabaseModule {

    public MockDatabaseModule() {
    }

    @Provides
    @Singleton
    public UserDatabaseHelper provideMockDatabaseHelper() {
        return Mockito.mock(UserDatabaseHelper.class);
    }

    @Provides
    @Named("local")
    @Singleton
    public WalletService provideMockWalletService() {
        WalletService mock = Mockito.mock(WalletService.class);
        List<Wallet> walletList = Lists.newArrayList(
                new Wallet().setId(0L).setName("wallet-1").setInitialAmount(100.0).setCurrentAmount(50.0),
                new Wallet().setId(10L).setName("wallet-2").setInitialAmount(200.0).setCurrentAmount(500.0),
                new Wallet().setId(20L).setName("wallet-3").setInitialAmount(500.0).setCurrentAmount(1200.0));

        Mockito.when(mock.getMyWallets()).thenReturn(walletList);
        Mockito.when(mock.findById(walletList.get(0).getId())).thenReturn(walletList.get(0));
        Mockito.when(mock.findById(walletList.get(1).getId())).thenReturn(walletList.get(1));
        Mockito.when(mock.findById(walletList.get(2).getId())).thenReturn(walletList.get(2));
        return mock;
    }
}
