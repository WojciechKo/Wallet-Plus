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
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.ui.DrawerListAdapter;
import info.korzeniowski.walletplus.ui.MainDrawerContent;

/**
 * Module for common objects.
 */
@Module(
        includes = DatabaseModule.class,
        injects = {
                MainActivity.class,
                DrawerListAdapter.class,
                MainDrawerContent.class
        }
)
public class MainModule {
    private final WalletPlus application;

    public MainModule(WalletPlus application) {
        this.application = application;
    }

    @Provides
    Context provideContext() {
        return application.getApplicationContext();
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
}
