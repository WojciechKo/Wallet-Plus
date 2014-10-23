package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

public class CashFlowTransferDetailsFragment extends CashFlowBaseDetailsFragment {

//    @InjectView(R.id.categoryLabel)
//    TextView categoryLabel;

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
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        cashFlowDetailsState.setExpanseFromWallet(selected);
    }

    @Override
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        cashFlowDetailsState.setIncomeToWallet(selected);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        category.setVisibility(View.GONE);
//        categoryLabel.setVisibility(View.GONE);
        removeCategory.setVisibility(View.GONE);
        return view;
    }
}
