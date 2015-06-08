package com.walletudo.dagger.module;

import android.content.Context;

import com.google.common.collect.Maps;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.ui.BaseActivity;
import com.walletudo.ui.cashflow.list.CashFlowListActivity;
import com.walletudo.ui.dashboard.DashboardActivity;
import com.walletudo.ui.statistics.StatisticActivity;
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
    private final WeakReference<Walletudo> application;

    public MainModule(Walletudo application) {
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
                new DrawerItemContent(R.drawable.ic_menu_dashboard, application.get().getString(R.string.dashboardMenu), DashboardActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.CASH_FLOW,
                new DrawerItemContent(R.drawable.ic_menu_cash_flow, application.get().getString(R.string.cashFlowMenu), CashFlowListActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.STATISTIC,
                new DrawerItemContent(R.drawable.ic_menu_statistic, application.get().getString(R.string.statisticsMenu), StatisticActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.WALLET,
                new DrawerItemContent(R.drawable.ic_menu_wallet, application.get().getString(R.string.walletMenu), WalletListActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.TAG,
                new DrawerItemContent(R.drawable.ic_menu_tag, application.get().getString(R.string.tagMenu), TagListActivity.class));
        navigationDrawerContent.put(BaseActivity.DrawerItemType.SYNCHRONIZE,
                new DrawerItemContent(R.drawable.ic_menu_synchronization, application.get().getString(R.string.synchronizationLabel), SynchronizeActivity.class));

        return navigationDrawerContent;
    }
}
