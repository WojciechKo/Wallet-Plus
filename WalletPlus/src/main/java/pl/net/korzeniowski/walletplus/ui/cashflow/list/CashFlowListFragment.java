package pl.net.korzeniowski.walletplus.ui.cashflow.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.WalletPlus;
import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsActivity;
import pl.net.korzeniowski.walletplus.util.KorzeniowskiUtils;

public class CashFlowListFragment extends Fragment {
    public static final String TAG = CashFlowListFragment.class.getSimpleName();

    @InjectView(R.id.list)
    ListView list;

    @Inject
    CashFlowService cashFlowService;

    private List<CashFlow> cashFlows;

    private List<CashFlow> selected;

    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).component().inject(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_cash_flow_list, container, false);
        ButterKnife.inject(this, view);
        setupList();
        return view;
    }

    void setupList() {
        cashFlows = cashFlowService.getAll();
        selected = Lists.newArrayList();
        list.setAdapter(new CashFlowListAdapter(getActivity(), cashFlows));
    }

    @OnItemLongClick(R.id.list)
    boolean listItemLongClicked(int position) {
        if (list.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) {
            startMultipleChoiceMode();
            KorzeniowskiUtils.Views.performItemClick(list, position);
            return true;

        } else if (list.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            KorzeniowskiUtils.Views.performItemClick(list, position);
            return true;
        }
        return false;
    }

    @OnItemClick(R.id.list)
    void listItemClicked(int position) {
        if (list.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) {
            Intent intent = new Intent(getActivity(), CashFlowDetailsActivity.class);
            intent.putExtra(CashFlowDetailsActivity.EXTRAS_CASH_FLOW_ID, list.getAdapter().getItemId(position));
            startActivityForResult(intent, CashFlowDetailsActivity.REQUEST_CODE_EDIT_CASH_FLOW);
            list.setItemChecked(position, false);

        } else if (list.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            CashFlow clickedCashFlow = cashFlows.get(position);

            if (!selected.remove(clickedCashFlow)) {
                selected.add(clickedCashFlow);
            }

            if (selected.size() == 0) {
                endMultipleChoiceMode();
            } else {
                getActivity().setTitle(getTitleWithSelectedCount());
            }
        }
    }

    private void startMultipleChoiceMode() {
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        ((CashFlowListActivity) getActivity()).getActionBarToolbar().setBackgroundColor(getResources().getColor(R.color.theme_primary_dark));

        title = getActivity().getTitle().toString();
        getActivity().setTitle(getTitleWithSelectedCount());

        ((CashFlowListActivity) getActivity()).getActionBarToolbar().getMenu().clear();
        ((CashFlowListActivity) getActivity()).getActionBarToolbar().inflateMenu(R.menu.action_delete);
    }

    private void endMultipleChoiceMode() {
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        ((CashFlowListActivity) getActivity()).getActionBarToolbar().setBackgroundColor(getResources().getColor(R.color.theme_primary));
        getActivity().setTitle(title);
        getActivity().supportInvalidateOptionsMenu();
    }

    private String getTitleWithSelectedCount() {
        return title + " (" + selected.size() + ")";
    }

    @Override
    public void onStop() {
        ((CashFlowListActivity) getActivity()).getActionBarToolbar().setBackgroundColor(getResources().getColor(R.color.theme_primary));
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            deleteSelectedCashFlows();
            return true;
        } else if (item.getItemId() == R.id.menu_switch) {
            if (item.isChecked()) {
                Predicate<CashFlow> notCompleted = new Predicate<CashFlow>() {
                    @Override
                    public boolean apply(CashFlow input) {
                        return !input.isCompleted();
                    }
                };
                list.setAdapter(new CashFlowListAdapter(getActivity(), Lists.newArrayList(Iterables.filter(cashFlows, notCompleted))));
            } else {
                list.setAdapter(new CashFlowListAdapter(getActivity(), cashFlows));
            }
        }
        return false;
    }

    private void deleteSelectedCashFlows() {
        for (CashFlow cashFlow : selected) {
            cashFlowService.deleteById(cashFlow.getId());
        }
        cashFlows.removeAll(selected);
        selected.clear();
        endMultipleChoiceMode();
        list.setAdapter(new CashFlowListAdapter(getActivity(), cashFlows));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CashFlowDetailsActivity.REQUEST_CODE_ADD_CASH_FLOW) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
            }
        } else if (requestCode == CashFlowDetailsActivity.REQUEST_CODE_EDIT_CASH_FLOW) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
                case CashFlowDetailsActivity.RESULT_DELETED:
                    setupList();
                    return;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}