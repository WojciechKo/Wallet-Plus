package info.korzeniowski.walletplus.test.module;

import org.robolectric.Robolectric;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.test.service.cashflow.LocalCashFlowServiceTest;
import info.korzeniowski.walletplus.test.service.category.LocalCategoryServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.LocalWalletServiceTest;

@Module(
        addsTo = DatabaseModule.class,
        injects = {
                LocalCashFlowServiceTest.class,
                LocalCategoryServiceTest.class,
                LocalWalletServiceTest.class
        },
        overrides = true,
        complete = false
)
public class TestDatabaseModule {

    private DatabaseHelper databaseHelper;

    public TestDatabaseModule() {
        databaseHelper = new DatabaseHelper(Robolectric.application, null);
    }

    @Provides
    public DatabaseHelper provideDatabaseHelper() {
        return databaseHelper;
    }
}
