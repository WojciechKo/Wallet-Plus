package com.walletudo;

import android.app.Application;

import com.walletudo.dagger.AppComponent;
import com.walletudo.util.PrefUtils;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

public class WalletUDo extends Application {

    @Inject
    PrefUtils prefUtils;

    private AppComponent component;

    private static WalletUDo walletUDo;

    @Override
    public void onCreate() {
        super.onCreate();
        walletUDo = this;
        reinitializeObjectGraph();
        component().inject(this);
        JodaTimeAndroid.init(this);
        initExampleData();
    }

    public void reinitializeObjectGraph() {
        component = AppComponent.Initializer.init(false);
    }

    void initExampleData() {
        if (!prefUtils.isDataBootstrapDone()) {
            new DatabaseInitializer(this).createExampleAccountWithProfile();
            prefUtils.markDataBootstrapDone();
        }
    }

    public AppComponent component() {
        return component;
    }

    public static WalletUDo getInstance() {
        return walletUDo;
    }
}
