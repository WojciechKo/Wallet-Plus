package info.korzeniowski.walletplus;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.module.MainModule;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;

/**
 * Main Application class.
 */
public class WalletPlus extends Application {
    public static final String LOG_TAG = "WalletPlus";

    protected ObjectGraph graph;

    /**
     * Just for Dagger DI.
     */
    @Inject
    public WalletPlus() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(getModules().toArray());
        new DatabaseInitializer(this).initDatabaseAfterInstallation();
    }

    public void inject(Object object) {
        graph.inject(object);
    }

    protected List<Object> getModules() {
        List<Object> modules = new ArrayList<Object>();
        modules.add(new MainModule(this));
        modules.add(new DatabaseModule(this));
        return modules;
    }
}
