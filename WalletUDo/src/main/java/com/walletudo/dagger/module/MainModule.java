package com.walletudo.dagger.module;

import android.content.Context;

import com.google.common.collect.Maps;
import com.walletudo.WalletUDo;
import com.walletudo.ui.BaseActivity;
import com.walletudo.ui.cashflow.list.CashFlowListActivity;
import com.walletudo.ui.dashboard.DashboardActivity;
import com.walletudo.ui.statistics.list.StatisticListActivity;
import com.walletudo.ui.statistics.list.StatisticListActivityState;
import com.walletudo.ui.synchronize.SynchronizeActivity;
import com.walletudo.ui.tag.list.TagListActivity;
import com.walletudo.ui.wallets.list.WalletListActivity;
import com.walletudo.util.PrefUtils;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.walletudo.ui.BaseActivity.DrawerItemContent;

@Module
public class MainModule {
    private final WeakReference<WalletUDo> application;

    public MainModule(WalletUDo application) {
        this.application = new WeakReference<>(application);
    }

    @Provides
    @Singleton
    Context provideContext() {
        return application.get();
    }

    @Provides
    @Named("amount")
    @Singleton
    NumberFormat provideNumberFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("0.00", symbols);
    }

    @Provides
    @Singleton
    StatisticListActivityState provideTagListActivityState() {
        return new StatisticListActivityState();
    }

    @Provides
    @Singleton
    PrefUtils providePrefUtils() {
        return new PrefUtils(application.get());
    }

    @Provides
    @Singleton
    Map<BaseActivity.DrawerItemType, DrawerItemContent> provideNavigationDrawerMap() {
        Map<BaseActivity.DrawerItemType, DrawerItemContent> navigationDrawerContent = Maps.newHashMap();

        navigationDrawerContent.put(BaseActivity.DrawerItemType.DASHBOARD,
                new DrawerItemContent(android.R.drawable.ic_media_pause, "Dashboard", DashboardActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.CASH_FLOW,
                new DrawerItemContent(android.R.drawable.ic_lock_silent_mode, "Cash flows", CashFlowListActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.STATISTIC,
                new DrawerItemContent(android.R.drawable.ic_menu_camera, "Statistics", StatisticListActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.WALLET,
                new DrawerItemContent(android.R.drawable.ic_menu_week, "Wallets", WalletListActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.TAG,
                new DrawerItemContent(android.R.drawable.ic_menu_agenda, "Tags", TagListActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.SYNCHRONIZE,
                new DrawerItemContent(android.R.drawable.stat_notify_sync_noanim, "Synchronization", SynchronizeActivity.class));
        return navigationDrawerContent;
    }
}
