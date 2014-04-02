package info.korzeniowski.walletplus.drawermenu;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import javax.inject.Inject;

import info.korzeniowski.walletplus.WalletPlus;

/**
 * Adapter for items from Main Drawer Menu
 */
public class MainDrawerListAdapter extends BaseAdapter {

    @Inject
    MainDrawerContent mainDrawerContent;

    @Inject
    Context context;

    @Inject
    WalletPlus app;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MainDrawerItemView mainDrawerItemView;
        if (convertView == null) {
            mainDrawerItemView = MainDrawerItemView_.build(context);
        } else {
            mainDrawerItemView = (MainDrawerItemView) convertView;
        }
        mainDrawerItemView.bind(getItem(position));
        return mainDrawerItemView;
    }

    @Override
    public int getCount() {
        return mainDrawerContent.getCount();
    }

    @Override
    public MainDrawerItem getItem(int position) {
        return mainDrawerContent.getDrawerItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}