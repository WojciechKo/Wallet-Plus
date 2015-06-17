package com.walletudo.dagger.module;

import com.google.common.collect.Lists;
import com.walletudo.ui.NavigationDrawerHelper;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NavigationDrawerMenuModule {
    @Provides
    @Singleton
    List<NavigationDrawerHelper.DrawerItemType> provideNavigationDrawerList() {
        List<NavigationDrawerHelper.DrawerItemType> navigationDrawerItemList = Lists.newArrayList();
        navigationDrawerItemList.add(NavigationDrawerHelper.DrawerItemType.DASHBOARD);
        navigationDrawerItemList.add(NavigationDrawerHelper.DrawerItemType.SEPARATOR);
        navigationDrawerItemList.add(NavigationDrawerHelper.DrawerItemType.CASH_FLOW);
        navigationDrawerItemList.add(NavigationDrawerHelper.DrawerItemType.TAG);
        navigationDrawerItemList.add(NavigationDrawerHelper.DrawerItemType.STATISTIC);
        navigationDrawerItemList.add(NavigationDrawerHelper.DrawerItemType.WALLET);
        return navigationDrawerItemList;
    }
}
