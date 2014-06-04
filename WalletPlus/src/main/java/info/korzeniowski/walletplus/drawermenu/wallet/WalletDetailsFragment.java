package info.korzeniowski.walletplus.drawermenu.wallet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

@OptionsMenu(R.menu.action_save)
@EFragment(R.layout.wallet_details_fragment)
public class WalletDetailsFragment extends Fragment {


    private enum DetailsType {ADD, EDIT;}
    static final String WALLET_ID = "WALLET_ID";
    @Inject
    @Named("local")
    WalletDataManager localWalletDataManager;

    private Long walletId;

    private Wallet wallet;
    private DetailsType type;
    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        walletId = getArguments().getLong(WALLET_ID);
        type = walletId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        wallet = getWallet();
        return null;
    }

    @AfterViews
    void setupViews() {
        Log.d("WalletPlus", "CategoryDetails.setupViews");
        setupAdapters();
        setupListeners();
        if (type.equals(DetailsType.EDIT)) {
            fillViewsWithData();
        }
    }

    private void setupListeners() {

    }

    private void setupAdapters() {

    }

    private void fillViewsWithData() {

    }

    private Wallet getWallet() {
        if (type.equals(DetailsType.EDIT)) {
            return localWalletDataManager.findById(walletId);
        }
        return new Wallet();
    }
}
