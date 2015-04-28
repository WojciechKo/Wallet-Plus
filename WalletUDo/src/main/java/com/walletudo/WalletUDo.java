package com.walletudo;

import android.app.Application;

import com.walletudo.dagger.AppComponent;

import net.danlew.android.joda.JodaTimeAndroid;

public class WalletUDo extends Application {

    private AppComponent component;

    private static WalletUDo walletUDo;

    @Override
    public void onCreate() {
        super.onCreate();
        walletUDo = this;
        reinitializeObjectGraph();
        JodaTimeAndroid.init(this);
    }

    public void reinitializeObjectGraph() {
        component = AppComponent.Initializer.init(false);
    }

    public AppComponent component() {
        return component;
    }

    public static WalletUDo getInstance() {
        return walletUDo;
    }
}
