package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment;

/**
 * Fragment with list of cash flows.
 */
@EFragment(R.layout.cash_flow_list)
@OptionsMenu(R.menu.action_new)
public class CashFlowListFragment extends Fragment {

    @ViewById(R.id.list)
    ListView list;

    @Inject @Named("local")
    CashFlowDataManager localCashFlowDataManager;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupView() {
        setHasOptionsMenu(true);
        list.setAdapter(new CashFlowListAdapter(getActivity(), android.R.layout.simple_list_item_1, localCashFlowDataManager.getAll()));
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Przytrzymano position: " + position + "id: " + id, Toast.LENGTH_LONG).show();
                ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeAfterLongPress(id));
                return false;
            }
        });
    }

    @OptionsItem(R.id.menu_new)
    void actionAdd() {
        Log.d("WalletPlus", "CategoryList.actionAdd");
        startCashFlowDetailsFragment();
    }

    private void startCashFlowDetailsFragment() {
        startCashFlowDetailsFragment(0L);
    }

    private void startCashFlowDetailsFragment(Long id) {
        Fragment fragment= new CashFlowDetailsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true);
    }

    private final class ActionModeAfterLongPress implements ActionMode.Callback {

        private final Long id;

        public ActionModeAfterLongPress(Long id) {
            this.id = id;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.action_edit_delete, menu);
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch(menuItem.getItemId()) {
                case R.id.menu_edit:
                    startCashFlowDetailsFragment(id);
                    break;
                case R.id.menu_delete:
                    localCashFlowDataManager.deleteById(id);
                    break;
            }
            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }

    }
}