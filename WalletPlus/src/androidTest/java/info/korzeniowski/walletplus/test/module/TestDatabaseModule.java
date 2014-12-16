package info.korzeniowski.walletplus.test.module;

import org.robolectric.Robolectric;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.local.UserDatabaseHelper;
import info.korzeniowski.walletplus.test.service.cashflow.LocalCashFlowServiceTest;
import info.korzeniowski.walletplus.test.service.category.LocalCategoryServiceTest;
import info.korzeniowski.walletplus.test.service.category.CategoryStatisticsTest;
import info.korzeniowski.walletplus.test.service.wallet.LocalWalletServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.WalletValidatorTest;

@Module(
        addsTo = DatabaseModule.class,
        injects = {
                LocalCashFlowServiceTest.class,
                CategoryStatisticsTest.class,
                LocalCategoryServiceTest.class,
                LocalWalletServiceTest.class,
                WalletValidatorTest.class
        },
        overrides = true,
        complete = false
)
public class TestDatabaseModule {

    private final UserDatabaseHelper userDatabaseHelper;

    public TestDatabaseModule() {
        userDatabaseHelper = new UserDatabaseHelper(Robolectric.application);
    }

    @Provides
    public UserDatabaseHelper provideDatabaseHelper() {
        return userDatabaseHelper;
    }
}
