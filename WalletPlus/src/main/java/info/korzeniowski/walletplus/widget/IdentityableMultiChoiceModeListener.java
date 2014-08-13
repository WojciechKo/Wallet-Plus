package info.korzeniowski.walletplus.widget;

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.common.collect.Maps;

import java.util.Map;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Identityable;
import info.korzeniowski.walletplus.service.BaseService;

public class IdentityableMultiChoiceModeListener<T extends Identityable> implements AbsListView.MultiChoiceModeListener {

    private final BaseService<T> service;
    private final Map<Identityable, View> selectedItemList;
    private final ListView listView;
    private final Activity activity;

    public IdentityableMultiChoiceModeListener(ListView listView, BaseService<T> service, Activity activity) {
        this.selectedItemList = Maps.newHashMap();
        this.listView = listView;
        this.service = service;
        this.activity = activity;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked) {
            selectedItemList.put((T) listView.getAdapter().getItem(position), listView.getSelectedView());
        } else {
            selectedItemList.remove(position);
        }
        mode.setSubtitle("(" + selectedItemList.size() + ")");
    }

    @Override
    public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
        mode.setTitle(activity.getTitle());
        mode.getMenuInflater().inflate(R.menu.action_delete, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_delete) {
            for (Map.Entry<Identityable, View> entry : selectedItemList.entrySet()) {
                service.deleteById(entry.getKey().getId());
                ((IdentityableListAdapter) listView.getAdapter()).remove(entry.getKey());
            }
            ((IdentityableListAdapter) listView.getAdapter()).notifyDataSetChanged();
            mode.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(android.view.ActionMode mode) {
        selectedItemList.clear();
    }
}