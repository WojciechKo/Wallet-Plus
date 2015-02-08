package info.korzeniowski.walletplus;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.module.MainModule;
import info.korzeniowski.walletplus.util.PrefUtils;

/**
 * Main Application class.
 */
public class WalletPlus extends Application {
    public static final String LOG_TAG = "WalletPlus";
    private static final String FIRST_RUN = "FIRST_RUN";
    private static final String LAST_LOGGED_PROFILE_ID = "LAST_LOGGED_PROFILE_ID";

    ObjectGraph graph;

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
        JodaTimeAndroid.init(this);
        initExampleData();
    }

    void initExampleData() {
        if (!PrefUtils.isDataBootstrapDone(this)) {
            new DatabaseInitializer(this).createExampleAccountWithProfile();
            PrefUtils.markDataBootstrapDone(this);
        }
    }

    public void inject(Object object) {
        graph.inject(object);
    }

    List<Object> getModules() {
        List<Object> modules = new ArrayList<>();
        modules.add(new MainModule(this));
        modules.add(new DatabaseModule(this));
        return modules;
    }

    public void reinitializeObjectGraph() {
        graph = ObjectGraph.create(getModules().toArray());
    }

    public ObjectGraph getGraph() {
        return graph;
    }
}
