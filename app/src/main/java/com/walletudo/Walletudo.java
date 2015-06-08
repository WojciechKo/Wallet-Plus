package com.walletudo;

import android.app.Application;

import com.walletudo.dagger.AppComponent;

import net.danlew.android.joda.JodaTimeAndroid;

public class Walletudo extends Application {

    private AppComponent component;

    private static Walletudo walletudo;

    @Override
    public void onCreate() {
        super.onCreate();
        walletudo = this;
        reinitializeObjectGraph();
        JodaTimeAndroid.init(this);
    }

    public void reinitializeObjectGraph() {
        component = AppComponent.Initializer.init(false);
    }

    public AppComponent component() {
        return component;
    }

    public static Walletudo getInstance() {
        return walletudo;
    }
}
