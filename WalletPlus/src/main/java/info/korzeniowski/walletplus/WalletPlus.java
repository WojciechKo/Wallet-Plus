package info.korzeniowski.walletplus;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.module.MainModule;

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
        initApplication();
    }

    void initApplication() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (isFirstRun(sharedPreferences)) {
            handleFirstRun(sharedPreferences);
        }
    }

    private boolean isFirstRun(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(FIRST_RUN, true);
    }

    private void handleFirstRun(SharedPreferences sharedPreferences) {
        new DatabaseInitializer(this).createExampleAccountWithProfile();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_RUN, false);
        editor.apply();
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
