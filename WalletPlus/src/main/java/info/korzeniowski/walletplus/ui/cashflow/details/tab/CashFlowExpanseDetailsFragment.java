package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsEvent;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsParcelableState;

public class CashFlowExpanseDetailsFragment extends CashFlowBaseDetailsFragment {
    @Override
    Wallet getFromWalletFromState() {
        return cashFlowDetailsState.getExpanseFromWallet();
    }

    @Override
    Wallet getToWalletFromState() {
        return cashFlowDetailsState.getExpanseToWallet();
    }

    @Override
    Category getCategoryFromState() {
        return cashFlowDetailsState.getExpanseCategory();
    }

    @Override
    void fillWalletLists() {
        fromWalletList.addAll(localWalletService.getMyWallets());
        toWalletList.addAll(localWalletService.getContractors());
    }

    @Override
    void fillCategoryList() {
        categoryList.addAll(localCategoryService.getMainExpenseTypeCategories());
    }

    @Override
    CashFlow getCashFlowFromState() {
        return new CashFlow(cashFlowDetailsState, CashFlow.Type.EXPANSE);
    }

    @Override
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        cashFlowDetailsState.setExpanseFromWallet(selected);
//        onCashFlowDetailsChangedListener.onFromWalletChanged();
        bus.post(new CashFlowDetailsEvent());
    }

    @Override
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        cashFlowDetailsState.setExpanseToWallet(selected);
//        onCashFlowDetailsChangedListener.onToWalletChanged();
        bus.post(new CashFlowDetailsEvent());
    }

    @Override
    void storeSelectedCategoryInState(Category category) {
        cashFlowDetailsState.setExpanseCategory(category);
    }

    @Override
    void onRemoveCategoryClick() {
        cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
//        onCashFlowDetailsChangedListener.onCategoryChanged();
        bus.post(new CashFlowDetailsEvent());
    }

    @Override
    protected void initState(CashFlowDetailsParcelableState cashFlowDetailsState) {
        super.initState(cashFlowDetailsState);
        if (!cashFlowDetailsState.isInit()) {
            initStateFromDatabase();
            cashFlowDetailsState.setInit(true);
        }
    }

    private void initStateFromDatabase() {
        CashFlow cashFlow = localCashFlowService.findById(cashFlowDetailsState.getId());
        cashFlowDetailsState.setAmount(cashFlow.getAmount());
        cashFlowDetailsState.setComment(cashFlow.getComment());
        cashFlowDetailsState.setDate(cashFlow.getDateTime().getTime());

        cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
        cashFlowDetailsState.setExpanseCategory(cashFlow.getCategory());
        cashFlowDetailsState.setExpanseFromWallet(cashFlow.getFromWallet());
        cashFlowDetailsState.setExpanseToWallet(cashFlow.getToWallet());
    }

}
