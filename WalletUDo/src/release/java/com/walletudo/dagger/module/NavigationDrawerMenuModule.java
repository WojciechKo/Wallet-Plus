package com.walletudo.dagger.module;

import com.google.common.collect.Lists;
import com.walletudo.ui.BaseActivity;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NavigationDrawerMenuModule {
    @Provides
    @Singleton
    List<BaseActivity.DrawerItemType> provideNavigationDrawerList() {
        List<BaseActivity.DrawerItemType> navigationDrawerItemList = Lists.newArrayList();
        navigationDrawerItemList.add(BaseActivity.DrawerItemType.DASHBOARD);
        navigationDrawerItemList.add(BaseActivity.DrawerItemType.SEPARATOR);
        navigationDrawerItemList.add(BaseActivity.DrawerItemType.CASH_FLOW);
        navigationDrawerItemList.add(BaseActivity.DrawerItemType.TAG);
        navigationDrawerItemList.add(BaseActivity.DrawerItemType.WALLET);
        navigationDrawerItemList.add(BaseActivity.DrawerItemType.STATISTIC);
        return navigationDrawerItemList;
    }
}
