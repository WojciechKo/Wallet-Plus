package com.walletudo.dagger.module;

import android.content.Context;

import com.walletudo.WalletUDo;
import com.walletudo.ui.statistics.list.StatisticListActivityState;
import com.walletudo.util.PrefUtils;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {
    private final WeakReference<WalletUDo> application;

    public MainModule(WalletUDo application) {
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
