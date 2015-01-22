package info.korzeniowski.walletplus.module;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.ui.category.list.CategoryListActivity;
import info.korzeniowski.walletplus.ui.category.list.CategoryListActivityState;
import info.korzeniowski.walletplus.ui.mywallets.details.MyWalletDetailsFragment;

/**
 * Module for common objects.
 */
@Module(
        includes = DatabaseModule.class,
        injects = {
                CategoryListActivity.class,

                MyWalletDetailsFragment.class
        }
)
public class MainModule {
    private final WalletPlus application;

    public MainModule(WalletPlus application) {
        this.application = application;
    }

    @Provides
    @Named("amount")
    @Singleton
    NumberFormat provideNumberFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("0.00", symbols);
    }

    @Provides
    @Singleton
    CategoryListActivityState provideCategoryListActivityState() {
        return new CategoryListActivityState();
    }
}
