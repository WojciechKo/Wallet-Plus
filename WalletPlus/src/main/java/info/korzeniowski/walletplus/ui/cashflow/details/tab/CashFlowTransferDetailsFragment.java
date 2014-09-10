package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsEvent;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsParcelableState;

public class CashFlowTransferDetailsFragment extends CashFlowBaseDetailsFragment {

    @Override
    Wallet getFromWalletFromState() {
        return cashFlowDetailsState.getExpanseFromWallet();
    }

    @Override
    Wallet getToWalletFromState() {
        return cashFlowDetailsState.getIncomeToWallet();
    }

    @Override
    Category getCategoryFromState() {
        return localCashFlowService.getTransferCategory();
    }

    @Override
    void fillWalletLists() {
        fromWalletList.addAll(localWalletService.getMyWallets());
        toWalletList.addAll(localWalletService.getMyWallets());
    }

    @Override
    void fillCategoryList() {
        return;
    }

    @Override
    CashFlow getCashFlowFromState() {
        CashFlow cashFlow = new CashFlow(cashFlowDetailsState, CashFlow.Type.TRANSFER);
        cashFlow.setCategory(localCashFlowService.getTransferCategory());
        return cashFlow;
    }

    @Override
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        cashFlowDetailsState.setExpanseFromWallet(selected);
        bus.post(new CashFlowDetailsEvent.FromWalletChanged());
//        onCashFlowDetailsChangedListener.onFromWalletChanged();
    }

    @Override
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        cashFlowDetailsState.setIncomeToWallet(selected);
        bus.post(new CashFlowDetailsEvent.ToWalletChanged());
//        onCashFlowDetailsChangedListener.onToWalletChanged();
    }

    @Override
    void storeSelectedCategoryInState(Category category) {
        return;
    }

    @Override
    void onRemoveCategoryClick() {
        return;
    }

    @Override
    protected void initState(CashFlowDetailsParcelableState cashFlowDetailsState) {
        super.initState(cashFlowDetailsState);
        if (!cashFlowDetailsState.isInit()) {
            CashFlow cashFlow = localCashFlowService.findById(cashFlowDetailsState.getId());
            cashFlowDetailsState.setAmount(cashFlow.getAmount());
            cashFlowDetailsState.setComment(cashFlow.getComment());
            cashFlowDetailsState.setDate(cashFlow.getDateTime().getTime());

            cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
            cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
            cashFlowDetailsState.setExpanseFromWallet(cashFlow.getFromWallet());
            cashFlowDetailsState.setIncomeToWallet(cashFlow.getToWallet());
            cashFlowDetailsState.setInit(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        category.setVisibility(View.GONE);
        return view;
    }
}
