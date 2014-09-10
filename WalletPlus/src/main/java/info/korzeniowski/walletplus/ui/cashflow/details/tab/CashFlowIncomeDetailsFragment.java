package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsEvent;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsParcelableState;

public class CashFlowIncomeDetailsFragment extends CashFlowBaseDetailsFragment {

    @Override
    Wallet getFromWalletFromState() {
        return cashFlowDetailsState.getIncomeFromWallet();
    }

    @Override
    Wallet getToWalletFromState() {
        return cashFlowDetailsState.getIncomeToWallet();
    }

    @Override
    Category getCategoryFromState() {
        return cashFlowDetailsState.getIncomeCategory();
    }

    @Override
    void fillWalletLists() {
        fromWalletList.addAll(localWalletService.getContractors());
        toWalletList.addAll(localWalletService.getMyWallets());
    }

    @Override
    void fillCategoryList() {
        categoryList.addAll(localCategoryService.getMainIncomeTypeCategories());
    }

    @Override
    CashFlow getCashFlowFromState() {
        return new CashFlow(cashFlowDetailsState, CashFlow.Type.INCOME);
    }

    @Override
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        cashFlowDetailsState.setIncomeFromWallet(selected);
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
        cashFlowDetailsState.setIncomeCategory(category);
    }

    @Override
    void onRemoveCategoryClick() {
        cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
        bus.post(new CashFlowDetailsEvent.CategoryChanged());
//        onCashFlowDetailsChangedListener.onCategoryChanged();
    }

    @Override
    protected void initState(CashFlowDetailsParcelableState cashFlowDetailsState) {
        super.initState(cashFlowDetailsState);
        if (!cashFlowDetailsState.isInit()) {
            CashFlow cashFlow = localCashFlowService.findById(cashFlowDetailsState.getId());
            cashFlowDetailsState.setAmount(cashFlow.getAmount());
            cashFlowDetailsState.setComment(cashFlow.getComment());
            cashFlowDetailsState.setDate(cashFlow.getDateTime().getTime());

            cashFlowDetailsState.setIncomeCategory(cashFlow.getCategory());
            cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
            cashFlowDetailsState.setIncomeFromWallet(cashFlow.getFromWallet());
            cashFlowDetailsState.setIncomeToWallet(cashFlow.getToWallet());
            cashFlowDetailsState.setInit(true);
        }
    }
}
