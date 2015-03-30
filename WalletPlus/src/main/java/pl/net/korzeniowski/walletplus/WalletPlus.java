package pl.net.korzeniowski.walletplus;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import pl.net.korzeniowski.walletplus.module.DatabaseModule;
import pl.net.korzeniowski.walletplus.module.GoogleDriveRestModule;
import pl.net.korzeniowski.walletplus.module.MainModule;
import pl.net.korzeniowski.walletplus.module.ServicesModule;
import pl.net.korzeniowski.walletplus.service.ProfileService;
import pl.net.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import pl.net.korzeniowski.walletplus.ui.BaseActivity;
import pl.net.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsActivity;
import pl.net.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsFragment;
import pl.net.korzeniowski.walletplus.ui.cashflow.list.CashFlowListActivity;
import pl.net.korzeniowski.walletplus.ui.cashflow.list.CashFlowListFragment;
import pl.net.korzeniowski.walletplus.ui.dashboard.DashboardActivity;
import pl.net.korzeniowski.walletplus.ui.dashboard.DashboardFragment;
import pl.net.korzeniowski.walletplus.ui.profile.ProfileActivity;
import pl.net.korzeniowski.walletplus.ui.statistics.details.StaticticDetailsActivity;
import pl.net.korzeniowski.walletplus.ui.statistics.details.StatisticDetailsFragment;
import pl.net.korzeniowski.walletplus.ui.statistics.list.StatisticListActivity;
import pl.net.korzeniowski.walletplus.ui.statistics.list.StatisticListFragment;
import pl.net.korzeniowski.walletplus.ui.synchronize.SynchronizeActivity;
import pl.net.korzeniowski.walletplus.ui.tag.details.TagDetailsActivity;
import pl.net.korzeniowski.walletplus.ui.tag.details.TagDetailsFragment;
import pl.net.korzeniowski.walletplus.ui.tag.list.TagListActivity;
import pl.net.korzeniowski.walletplus.ui.tag.list.TagListFragment;
import pl.net.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;
import pl.net.korzeniowski.walletplus.ui.wallets.details.WalletDetailsFragment;
import pl.net.korzeniowski.walletplus.ui.wallets.list.WalletListActivity;
import pl.net.korzeniowski.walletplus.ui.wallets.list.WalletListFragment;
import pl.net.korzeniowski.walletplus.util.PrefUtils;

/**
 * Main Application class.
 */
public class WalletPlus extends Application {

    @Singleton
    @Component(
            modules = {
                    MainModule.class,
                    DatabaseModule.class,
                    ServicesModule.class,
                    GoogleDriveRestModule.class}
    )

    public interface RealComponent extends ApplicationComponent {

    }

    public interface ApplicationComponent {
        void inject(WalletPlus object);

        void inject(BaseActivity object);

        void inject(MainActivity object);

        void inject(DashboardActivity object);

        void inject(DashboardFragment object);

        void inject(StatisticListActivity object);

        void inject(StatisticListFragment object);

        void inject(StaticticDetailsActivity object);

        void inject(StatisticDetailsFragment object);

        void inject(CashFlowListActivity object);

        void inject(CashFlowListFragment object);

        void inject(CashFlowDetailsActivity object);

        void inject(CashFlowDetailsFragment object);

        void inject(WalletListActivity object);

        void inject(WalletListFragment object);

        void inject(WalletDetailsActivity object);

        void inject(WalletDetailsFragment object);

        void inject(TagListActivity object);

        void inject(TagListFragment object);

        void inject(TagDetailsActivity object);

        void inject(TagDetailsFragment object);

        void inject(SynchronizeActivity object);

        void inject(SynchronizeActivity.SynchronizeFragment object);

        void inject(ProfileActivity object);

        void inject(ProfileActivity.CreateProfileFragment object);

        void inject(DatabaseInitializer object);

        void inject(ProfileServiceOrmLite object);

        ProfileService profileService();
    }

    @Inject
    PrefUtils prefUtils;

    ApplicationComponent component;

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
        component = Dagger_WalletPlus_RealComponent.builder()
                .mainModule(new MainModule(this))
                .build();
    }

    void initExampleData() {
        if (!prefUtils.isDataBootstrapDone()) {
            new DatabaseInitializer(this).createExampleAccountWithProfile();
            prefUtils.markDataBootstrapDone();
        }
    }

    public ApplicationComponent component() {
        return component;
    }

    public static WalletPlus getInstance() {
        return walletPlus;
    }
}
