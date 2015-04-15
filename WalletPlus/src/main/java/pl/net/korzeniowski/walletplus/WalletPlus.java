package pl.net.korzeniowski.walletplus;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.dagger.AppComponent;
import pl.net.korzeniowski.walletplus.util.PrefUtils;

public class WalletPlus extends Application {

    @Inject
    PrefUtils prefUtils;

    private AppComponent component;

    private static WalletPlus walletPlus;

    @Override
    public void onCreate() {
        super.onCreate();
        walletPlus = this;
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

    public static WalletPlus getInstance() {
        return walletPlus;
    }
}
