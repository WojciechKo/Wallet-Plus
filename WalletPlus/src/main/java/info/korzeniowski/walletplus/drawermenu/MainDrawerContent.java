package info.korzeniowski.walletplus.drawermenu;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.drawermenu.category.CategoryFragment;
import info.korzeniowski.walletplus.drawermenu.dashboard.DashboardFragment_;

/**
 * Content of Main Drawer Menu.
 */
@Singleton
public class MainDrawerContent {
    private List<MainDrawerItem> mainDrawerItems;

    @Inject
    public MainDrawerContent(Context context) {
        mainDrawerItems = new LinkedList<MainDrawerItem>();
        mainDrawerItems.add(new MainDrawerItem(
                                context.getString(R.string.app_name),
                                R.drawable.ic_menu_dashboard,
                                DashboardFragment_.class
                        )
        );
        mainDrawerItems.add(new MainDrawerItem(
                                context.getString(R.string.categories_title),
                                R.drawable.ic_menu_categories,
                                CategoryFragment.class
                        )
        );
    }

    public MainDrawerItem getDrawerItem(int position) {
        return mainDrawerItems.get(position);
    }

    public int getCount() {
        return mainDrawerItems.size();
    }
}
