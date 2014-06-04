package info.korzeniowski.walletplus.drawermenu.wallet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.exception.WalletNameAndTypeMustBeUniqueException;
import info.korzeniowski.walletplus.model.Wallet;

@OptionsMenu(R.menu.action_save)
@EFragment(R.layout.wallet_details_fragment)
public class WalletDetailsFragment extends Fragment {
    private enum DetailsType {ADD, EDIT;}
    static final String WALLET_ID = "WALLET_ID";

    @ViewById
    TextView walletName;

    @ViewById
    TextView walletInitialAmount;

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
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    private void setupListeners() {
        walletName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateIfEmpty(v, getString(R.string.walletNameCantBeEmpty));
                }
            }
        });

        walletInitialAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateIfEmpty(v, getString(R.string.initialAmountCantBeEmpty));
                }
            }
        });
    }

    private void validateIfEmpty(View view, String errorMsg) {
        TextView textView = (TextView) view;
        if (Strings.isNullOrEmpty(textView.getText().toString())) {
            textView.setError(errorMsg);
        }
    }

    private void setupAdapters() {

    }

    private void fillViewsWithData() {
        if (type.equals(DetailsType.EDIT)) {
            walletName.setText(wallet.getName());
            walletInitialAmount.setText(wallet.getInitialAmount().toString());
        }
    }

    private void getDataFromViews() {
        wallet.setName(walletName.getText().toString());
        String initialAmount = walletInitialAmount.getText().toString();
        wallet.setInitialAmount(Double.parseDouble(initialAmount));
    }

    private Wallet getWallet() {
        if (type.equals(DetailsType.EDIT)) {
            return localWalletDataManager.findById(walletId);
        }
        return new Wallet();
    }

    @OptionsItem(R.id.menu_save)
    void actionSave() {
        if (validate()) {
            getDataFromViews();
            wallet.setType(Wallet.Type.MY_WALLET);
            try {
                if (DetailsType.ADD.equals(type)) {
                    localWalletDataManager.insert(wallet);
                } else if (DetailsType.EDIT.equals(type)) {
                    localWalletDataManager.update(wallet);
                }
                getActivity().getSupportFragmentManager().popBackStack();
            } catch (WalletNameAndTypeMustBeUniqueException e) {
                walletName.setError("Wallet name have to be unique.");
            }
        }
    }

    private boolean validate() {
        validateIfEmpty(walletName, getString(R.string.walletNameCantBeEmpty));
        validateIfEmpty(walletInitialAmount, getString(R.string.initialAmountCantBeEmpty));
        return !isAnyErrorsAppear();
    }
    private boolean isAnyErrorsAppear() {
        if (walletName.getError() != null) {
            return true;
        }
        if (walletInitialAmount.getError() != null) {
            return true;
        }
        return false;
    }
}
