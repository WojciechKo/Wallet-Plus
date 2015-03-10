package info.korzeniowski.walletplus;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.module.MainModule;
import info.korzeniowski.walletplus.service.ormlite.AccountServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsActivity;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListActivity;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListFragment;
import info.korzeniowski.walletplus.ui.dashboard.DashboardActivity;
import info.korzeniowski.walletplus.ui.dashboard.DashboardFragment;
import info.korzeniowski.walletplus.ui.profile.ProfileActivity;
import info.korzeniowski.walletplus.ui.statistics.details.StaticticDetailsActivity;
import info.korzeniowski.walletplus.ui.statistics.details.StatisticDetailsFragment;
import info.korzeniowski.walletplus.ui.statistics.list.StatisticListActivity;
import info.korzeniowski.walletplus.ui.statistics.list.StatisticListFragment;
import info.korzeniowski.walletplus.ui.synchronize.SynchronizeActivity;
import info.korzeniowski.walletplus.ui.tag.details.TagDetailsActivity;
import info.korzeniowski.walletplus.ui.tag.details.TagDetailsFragment;
import info.korzeniowski.walletplus.ui.tag.list.TagListActivity;
import info.korzeniowski.walletplus.ui.tag.list.TagListFragment;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsFragment;
import info.korzeniowski.walletplus.ui.wallets.list.WalletListActivity;
import info.korzeniowski.walletplus.ui.wallets.list.WalletListFragment;
import info.korzeniowski.walletplus.util.PrefUtils;

/**
 * Main Application class.
 */
public class WalletPlus extends Application {

    @Singleton
    @Component(modules = {MainModule.class,
            DatabaseModule.class})
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

        void inject(AccountServiceOrmLite object);

        void inject(ProfileServiceOrmLite object);
    }

    @Inject
    PrefUtils prefUtils;

    ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = Dagger_WalletPlus_ApplicationComponent.builder()
                .databaseModule(new DatabaseModule(this))
                .mainModule(new MainModule(this))
                .build();
        component().inject(this);
        JodaTimeAndroid.init(this);
        initExampleData();
    }

    public void reinitializeObjectGraph() {
        component = Dagger_WalletPlus_ApplicationComponent.builder()
                .databaseModule(new DatabaseModule(this))
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
}
