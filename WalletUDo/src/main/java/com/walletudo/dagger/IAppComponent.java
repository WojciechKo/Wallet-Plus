package com.walletudo.dagger;

import com.walletudo.DatabaseInitializer;
import com.walletudo.MainActivity;
import com.walletudo.Walletudo;
import com.walletudo.service.ProfileService;
import com.walletudo.service.ormlite.ProfileServiceOrmLite;
import com.walletudo.ui.BaseActivity;
import com.walletudo.ui.cashflow.details.CashFlowDetailsActivity;
import com.walletudo.ui.cashflow.details.CashFlowDetailsFragment;
import com.walletudo.ui.cashflow.list.CashFlowListActivity;
import com.walletudo.ui.cashflow.list.CashFlowListFragment;
import com.walletudo.ui.dashboard.DashboardActivity;
import com.walletudo.ui.dashboard.DashboardFragment;
import com.walletudo.ui.profile.ProfileActivity;
import com.walletudo.ui.settings.SettingsActivity;
import com.walletudo.ui.statistics.StatisticFragment;
import com.walletudo.ui.statistics.details.StaticticDetailsActivity;
import com.walletudo.ui.statistics.details.StatisticDetailsFragment;
import com.walletudo.ui.statistics.list.StatisticListActivity;
import com.walletudo.ui.statistics.list.StatisticListFragment;
import com.walletudo.ui.synchronize.SynchronizeActivity;
import com.walletudo.ui.tag.details.TagDetailsActivity;
import com.walletudo.ui.tag.details.TagDetailsFragment;
import com.walletudo.ui.tag.list.TagListActivity;
import com.walletudo.ui.tag.list.TagListFragment;
import com.walletudo.ui.wallets.details.WalletDetailsActivity;
import com.walletudo.ui.wallets.details.WalletDetailsFragment;
import com.walletudo.ui.wallets.list.WalletListActivity;
import com.walletudo.ui.wallets.list.WalletListFragment;
import com.walletudo.util.PrefUtils;

public interface IAppComponent {

    void inject(Walletudo object);

    void inject(BaseActivity object);

    void inject(MainActivity object);

    void inject(DashboardActivity object);

    void inject(DashboardFragment object);

    void inject(StatisticFragment statisticFragment);

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

    void inject(SettingsActivity.SettingsFragment settingsFragment);

    ProfileService profileService();

    PrefUtils prefUtils();
}
