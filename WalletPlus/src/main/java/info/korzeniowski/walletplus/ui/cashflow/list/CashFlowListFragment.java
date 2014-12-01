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
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import info.korzeniowski.walletplus.KorzeniowskiUtils;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsContainerFragment;

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

    private List<CashFlow> cashFlows;
    private List<CashFlow> selected;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.cashflow_list, container, false);
        ButterKnife.inject(this, view);
        cashFlows = localCashFlowService.getAll();
        selected = Lists.newArrayList();
        setupView();
        return view;
    }

    void setupView() {
        list.setAdapter(new CashFlowListAdapter(getActivity(), cashFlows));
    }

    @OnItemClick(R.id.list)
    void listItemClicked(int position) {
        View itemView = KorzeniowskiUtils.Views.getViewByPosition(list, position);
        if (list.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) {
            startCashFlowDetailsFragment(list.getAdapter().getItemId(position));
            list.setItemChecked(position, false);
        } else if (list.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            handleCategorySelect(position, itemView);
            if (selected.size() == 0) {
                endMultipleChoiceMode();
            } else {
                getActivity().setTitle(getSelectedTitle());
            }
        }
    }

    @OnItemLongClick(R.id.list)
    boolean listItemLongClicked(int position) {
        if (list.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) {
            startMultipleChoiceMode(position);
            KorzeniowskiUtils.Views.performItemClick(list, position);
        } else if (list.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            KorzeniowskiUtils.Views.performItemClick(list, position);
        }
        return true;
    }

    private void handleCategorySelect(int position, View itemView) {
        if (selected.contains(cashFlows.get(position))) {
            selected.remove(cashFlows.get(position));
        } else {
            selected.add(cashFlows.get(position));
        }
    }

    private void startMultipleChoiceMode(int position) {
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        ((MainActivity) getActivity()).setToolbarBackground(getResources().getColor(R.color.darkerMainColor));

        title = getActivity().getTitle().toString();
        getActivity().setTitle(getSelectedTitle());

        ((MainActivity) getActivity()).getToolbar().getMenu().clear();
        ((MainActivity) getActivity()).getToolbar().inflateMenu(R.menu.action_delete);
    }

    private void endMultipleChoiceMode() {
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        ((MainActivity) getActivity()).setToolbarBackground(getResources().getColor(R.color.mainColor));
        getActivity().setTitle(title);
        ((MainActivity) getActivity()).getToolbar().getMenu().clear();
        onCreateOptionsMenu(((MainActivity) getActivity()).getToolbar().getMenu(), getActivity().getMenuInflater());
    }

    @Override
    public void onStop() {
        ((MainActivity) getActivity()).setToolbarBackground(getResources().getColor(R.color.mainColor));
        super.onStop();
    }

    private String getSelectedTitle() {
        return title + " (" + selected.size() + ")";
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
        } else if (item.getItemId() == R.id.menu_delete) {
            deleteSelectedCashFlows();
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

    private void deleteSelectedCashFlows() {
        for (CashFlow cashFlow : selected) {
            localCashFlowService.deleteById(cashFlow.getId());
        }
        cashFlows.removeAll(selected);
        selected.clear();
        endMultipleChoiceMode();
        list.setAdapter(new CashFlowListAdapter(getActivity(), cashFlows));
    }
}