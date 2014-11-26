package info.korzeniowski.walletplus.ui.cashflow.list;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsContainerFragment;
import info.korzeniowski.walletplus.widget.IdentifiableMultiChoiceModeListener;

/**
 * Fragment with list of cash flows.
 */
public class CashFlowListFragment extends Fragment {
    public static final String TAG = "cashFlowList";

    @InjectView(R.id.list)
    ListView list;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.card_list, container, false);
        ButterKnife.inject(this, view);
        setupView();
        return view;
    }

    void setupView() {
        list.setAdapter(new CashFlowListAdapter(getActivity(), localCashFlowService.getAll()));
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new IdentifiableMultiChoiceModeListener<CashFlow>(list, localCashFlowService, getActivity()));
    }

    @OnItemClick(R.id.list)
    void listItemClicked(int position) {
        startCashFlowDetailsFragment(list.getAdapter().getItemId(position));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            startCashFlowDetailsFragment();
            return true;
        }
        return false;
    }

    private void startCashFlowDetailsFragment() {
        ((MainActivity) getActivity()).setContentFragment(new CashFlowDetailsContainerFragment(), true, CashFlowDetailsContainerFragment.TAG);
    }

    private void startCashFlowDetailsFragment(Long id) {
        Fragment fragment = new CashFlowDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CashFlowDetailsContainerFragment.CASH_FLOW_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true, CashFlowDetailsContainerFragment.TAG);
    }
}