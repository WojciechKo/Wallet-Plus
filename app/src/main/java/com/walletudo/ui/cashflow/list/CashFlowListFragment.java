package com.walletudo.ui.cashflow.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.diegocarloslima.fgelv.lib.FloatingGroupExpandableListView;
import com.diegocarloslima.fgelv.lib.WrapperExpandableListAdapter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.model.CashFlow;
import com.walletudo.service.CashFlowService;
import com.walletudo.ui.cashflow.details.CashFlowDetailsActivity;
import com.walletudo.util.StateManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashFlowListFragment extends Fragment {
    public static final String TAG = CashFlowListFragment.class.getSimpleName();

    @InjectView(R.id.list)
    FloatingGroupExpandableListView list;

    @Inject
    CashFlowService cashFlowService;

    private List<CashFlow> cashFlows;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Walletudo) getActivity().getApplication()).component().inject(this);
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

    @Override
    public void onPause() {
        super.onPause();
        int listItem = list.getFirstVisiblePosition();
        View itemView = list.getChildAt(0);
        int listItemScroll = itemView == null ? 0 : itemView.getTop();
        StateManager.CashFlowListState listState = new StateManager.CashFlowListState(listItem, listItemScroll);

        StateManager.setCashFlowListState(getActivity(), listState);
    }

    @Override
    public void onStart() {
        super.onStart();
        StateManager.CashFlowListState state = StateManager.getCashFlowListState(getActivity());
        list.setSelectionFromTop(state.getListItem(), state.getListItemScroll());
    }

    void setupList() {
        cashFlows = cashFlowService.getAll();
        setupList(cashFlows);
    }

    void setupList(List<CashFlow> cashFlows) {
        CashFlowListAdapter adapter = new CashFlowListAdapter(getActivity(), cashFlows);
        list.setAdapter(new WrapperExpandableListAdapter(adapter));
        list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return onChildClicks(groupPosition, childPosition);
            }
        });

        int count = list.getExpandableListAdapter().getGroupCount();
        for (int index = 0; index < count; index++) {
            list.expandGroup(index);
        }
    }

    private boolean onChildClicks(int groupPosition, int childPosition) {
        Intent intent = new Intent(getActivity(), CashFlowDetailsActivity.class);
        long childId = list.getExpandableListAdapter().getChildId(groupPosition, childPosition);
        intent.putExtra(CashFlowDetailsActivity.EXTRAS_CASH_FLOW_ID, childId);
        startActivityForResult(intent, CashFlowDetailsActivity.REQUEST_CODE_EDIT_CASH_FLOW);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_switch) {
            List<CashFlow> notCompletedCashFlows;
            if (item.isChecked()) {
                Predicate<CashFlow> notCompleted = new Predicate<CashFlow>() {
                    @Override
                    public boolean apply(CashFlow input) {
                        return !input.isCompleted();
                    }
                };
                notCompletedCashFlows = Lists.newArrayList(Iterables.filter(cashFlows, notCompleted));
            } else {
                notCompletedCashFlows = this.cashFlows;
            }

            setupList(notCompletedCashFlows);
        }
        return false;
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