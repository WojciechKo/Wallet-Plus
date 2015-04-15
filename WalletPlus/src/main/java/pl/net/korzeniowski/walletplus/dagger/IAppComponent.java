package pl.net.korzeniowski.walletplus.dagger;

import pl.net.korzeniowski.walletplus.DatabaseInitializer;
import pl.net.korzeniowski.walletplus.MainActivity;
import pl.net.korzeniowski.walletplus.WalletPlus;
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

public interface IAppComponent {

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
