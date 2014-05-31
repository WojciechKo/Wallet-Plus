package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.model.CashFlow;

@EFragment(R.layout.cash_flow_details_fragment)
@OptionsMenu(R.menu.action_save)
public class CashFlowDetailsFragment extends Fragment {
    private enum DetailsType {ADD, EDIT}
    static final public String CASH_FLOW_ID = "CASH_FLOW_ID";
    @Inject @Named("local")
    CashFlowDataManager localCashFlowDataManager;
    private Long cashFlowId;
    private DetailsType type;
    private CashFlow cashFlow;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cashFlowId = getArguments().getLong(CASH_FLOW_ID);
        type = cashFlowId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        if (type.equals(DetailsType.EDIT)) {
            cashFlow = getCashFlow();
        }
        return null;
    }

    private CashFlow getCashFlow() {
        return localCashFlowDataManager.findById(cashFlowId);
    }

    @AfterViews
    void setupViews() {
        Log.d("WalletPlus", "CategoryDetails.setupViews");
        setupAdapters();
        setupListeners();
        if (type.equals(DetailsType.ADD)) {
            cashFlow = new CashFlow();
        } else if (type.equals(DetailsType.EDIT)) {
            fillViewsWithData();
        }
    }

    private void setupAdapters() {

    }

    private void setupListeners() {

    }

    private void fillViewsWithData() {

    }

    public void getDataFromViews() {

    }
    @OptionsItem(R.id.menu_save)
    void actionSave() {
        Log.d("WalletPlus", "CategoryDetails.actionSave");
        getDataFromViews();
        if (DetailsType.ADD.equals(type)) {
            localCashFlowDataManager.insert(cashFlow);
        } else if (DetailsType.EDIT.equals(type)) {
            localCashFlowDataManager.update(cashFlow);
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }


}
