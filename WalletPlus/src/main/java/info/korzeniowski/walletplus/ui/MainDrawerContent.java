package info.korzeniowski.walletplus.ui;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListFragment;
import info.korzeniowski.walletplus.ui.category.list.CategoryListFragmentMain;
import info.korzeniowski.walletplus.ui.dashboard.DashboardFragment;
import info.korzeniowski.walletplus.ui.wallet.list.WalletListFragment;

/**
 * Content of Main Drawer Menu.
 */
@Singleton
public class MainDrawerContent {
    private final List<MainDrawerItem> mainDrawerItems;

    @Inject
    public MainDrawerContent(Context context) {
        mainDrawerItems = new LinkedList<MainDrawerItem>();

        mainDrawerItems.add(
                new MainDrawerItem(
                        context.getString(R.string.appName),
                        R.drawable.ic_menu_dashboard,
                        DashboardFragment.class,
                        DashboardFragment.TAG));

        mainDrawerItems.add(
                new MainDrawerItem(
                        context.getString(R.string.cashflowMenu),
                        R.drawable.ic_menu_cashflow,
                        CashFlowListFragment.class,
                        CashFlowListFragment.TAG));

        mainDrawerItems.add(
                new MainDrawerItem(
                        context.getString(R.string.categoryMenu),
                        R.drawable.ic_menu_categories,
                        CategoryListFragmentMain.class,
                        CategoryListFragmentMain.TAG));

        mainDrawerItems.add(
                new MainDrawerItem(
                        context.getString(R.string.walletMenu),
                        R.drawable.ic_menu_wallets,
                        WalletListFragment.class,
                        WalletListFragment.TAG));
    }

    public MainDrawerItem getDrawerItem(int position) {
        return mainDrawerItems.get(position);
    }

    public int getCount() {
        return mainDrawerItems.size();
    }
}
