package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.service.CashFlowService;

/**
 * Fragment with list of cash flows.
 */
@OptionsMenu(R.menu.action_new)
@EFragment(R.layout.card_list)
public class CashFlowListFragment extends Fragment {

    @ViewById
    ListView list;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupView() {
        setHasOptionsMenu(true);
        list.setAdapter((new CashFlowListAdapter(getActivity(), localCashFlowService.getAll())));
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeAfterLongPress(id));
                return true;
            }
        });
    }

    @OptionsItem(R.id.menu_new)
    void actionAdd() {
        startCashFlowDetailsFragment();
    }

    private void startCashFlowDetailsFragment() {
        startCashFlowDetailsFragment(0L);
    }

    private void startCashFlowDetailsFragment(Long id) {
        Fragment fragment = new CashFlowDetailsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(CashFlowDetailsFragment_.CASH_FLOW_ID, id);
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
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_edit:
                    startCashFlowDetailsFragment(id);
                    break;
                case R.id.menu_delete:
                    localCashFlowService.deleteById(id);
                    list.setAdapter((new CashFlowListAdapter(getActivity(), localCashFlowService.getAll())));
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