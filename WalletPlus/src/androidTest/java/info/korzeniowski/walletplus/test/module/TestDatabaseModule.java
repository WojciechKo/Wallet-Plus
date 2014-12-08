package info.korzeniowski.walletplus.test.module;

import org.robolectric.Robolectric;

import java.sql.SQLException;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
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

    private DatabaseHelper databaseHelper;

    public TestDatabaseModule() {
        databaseHelper = new DatabaseHelper(Robolectric.application, null);
        try {
            databaseHelper.getCategoryDao().create(new Category().setType(Category.Type.TRANSFER).setName("Transfer"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Provides
    public DatabaseHelper provideDatabaseHelper() {
        return databaseHelper;
    }
}
