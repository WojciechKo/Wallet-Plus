package info.korzeniowski.walletplus.ui;

import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Simple class for holding menu items in MainDrawerMenu.
 */
public class MainDrawerItem {
    private final String title;
    private final int icon;
    private final Class<? extends Fragment> fragment;
    private final String tag;

    public MainDrawerItem(String title, int icon, Class<? extends Fragment> fragment, String tag) {
        this.title = title;
        this.icon = icon;
        this.fragment = fragment;
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public Fragment getFragment() {
        try {
            return fragment.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.w("WalletPlus", "Nie można stworzyć obiektu Fragment: " + e);
        }
        return null;
    }

    public String getTag() {
        return tag;
    }
}
