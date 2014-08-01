package info.korzeniowski.walletplus.module;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.MainActivity_;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.drawermenu.DrawerListAdapter;
import info.korzeniowski.walletplus.drawermenu.MainDrawerContent;

/**
 * Module for common objects.
 */
@Module(
        includes = DatabaseModule.class,
        injects = {
                MainActivity_.class,
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
}
