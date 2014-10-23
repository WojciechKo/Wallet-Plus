package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import com.squareup.otto.Subscribe;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsStatusChangedEvent;

public class CashFlowIncomeDetailsFragment extends CashFlowBaseDetailsFragment {

    @Subscribe
    public void statusChanged(CashFlowDetailsStatusChangedEvent event) {
        super.statusChanged();
    }

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
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        cashFlowDetailsState.setIncomeFromWallet(selected);
    }

    @Override
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        cashFlowDetailsState.setIncomeToWallet(selected);
    }

    @Override
    void storeSelectedCategoryInState(Category category) {
        cashFlowDetailsState.setIncomeCategory(category);
    }

    @Override
    void onRemoveCategoryClick() {
        cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
    }
}
