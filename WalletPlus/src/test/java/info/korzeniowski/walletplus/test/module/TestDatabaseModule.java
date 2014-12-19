package info.korzeniowski.walletplus.test.module;

import dagger.Module;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.test.service.cashflow.LocalCashFlowServiceTest;
import info.korzeniowski.walletplus.test.service.category.CategoryStatisticsTest;
import info.korzeniowski.walletplus.test.service.category.LocalCategoryServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.LocalWalletServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.WalletValidatorTest;

@Module(
        includes = DatabaseModule.class,
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
// Just to not list test classes in injects in DatabaseModule
public class TestDatabaseModule {

}
