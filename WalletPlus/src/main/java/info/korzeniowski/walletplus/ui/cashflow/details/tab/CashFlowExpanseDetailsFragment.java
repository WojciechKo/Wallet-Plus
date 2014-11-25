package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import com.squareup.otto.Subscribe;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsStatusChangedEvent;

public class CashFlowExpanseDetailsFragment extends CashFlowBaseDetailsFragment {

    @Subscribe
    public void statusChanged(CashFlowDetailsStatusChangedEvent event) {
        if (!event.getFragmentClass().equals(getClass())) {
            fillViewsFromState();
        }
    }

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
        categoryList.addAll(localCategoryService.getMainCategories());
    }

    @Override
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        cashFlowDetailsState.setExpanseFromWallet(selected);
    }

    @Override
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        cashFlowDetailsState.setExpanseToWallet(selected);
    }

    @Override
    void storeSelectedCategoryInState(Category category) {
        cashFlowDetailsState.setExpanseCategory(category);
    }

    @Override
    void onRemoveCategoryClick() {
        cashFlowDetailsState.setExpanseCategory(null);
    }
}
