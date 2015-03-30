package pl.net.korzeniowski.walletplus.module;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pl.net.korzeniowski.walletplus.WalletPlus;
import pl.net.korzeniowski.walletplus.ui.statistics.list.StatisticListActivityState;
import pl.net.korzeniowski.walletplus.util.PrefUtils;

@Module
public class MainModule {
    private final WeakReference<WalletPlus> application;

    public MainModule(WalletPlus application) {
        this.application = new WeakReference<>(application);
    }

    @Provides
    @Singleton
    Context provideContext() {
        return application.get();
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
    StatisticListActivityState provideTagListActivityState() {
        return new StatisticListActivityState();
    }

    @Provides
    @Singleton
    PrefUtils providePrefUtils() {
        return new PrefUtils(application.get());
    }
}
