package info.korzeniowski.walletplus.module;

import android.content.Context;

import com.squareup.otto.Bus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.ui.category.list.CategoryListActivityState;
import info.korzeniowski.walletplus.ui.wallet.details.WalletDetailsFragment;
import info.korzeniowski.walletplus.ui.wallet.list.WalletListAdapter;

/**
 * Module for common objects.
 */
@Module(
        includes = DatabaseModule.class,
        injects = {
                WalletListAdapter.class,

                WalletDetailsFragment.class
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
    Bus provideBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    CategoryListActivityState provideCategoryListActivityState() {
        return new CategoryListActivityState();
    }
}
