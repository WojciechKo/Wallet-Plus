package info.korzeniowski.walletplus.ui.wallet.details;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.text.NumberFormat;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;

public class WalletDetailsFragment extends Fragment {
    public static final String TAG = "walletDetails";
    public static final String WALLET_ID = "WALLET_ID";
    private static final String WALLET_DETAILS_STATE = "walletDetailsState";

    private enum DetailsType {ADD, EDIT}

    @InjectView(R.id.walletNameLabel)
    TextView walletNameLabel;

    @InjectView(R.id.walletName)
    EditText walletName;

    @InjectView(R.id.walletInitialAmountLabel)
    TextView walletInitialAmountLabel;

    @InjectView(R.id.walletInitialAmount)
    EditText walletInitialAmount;

    @InjectView(R.id.walletCurrentAmountLabel)
    TextView walletCurrentAmountLabel;

    @InjectView(R.id.walletCurrentAmount)
    TextView walletCurrentAmount;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Inject
    @Named("amount")
    NumberFormat amountFormat;

    private WalletDetailsParcelableState walletDetailsState;
    private DetailsType type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
//        walletDetailsState = initOrRestoreState(savedInstanceState);
        type = walletDetailsState.getId() == null ? DetailsType.ADD : DetailsType.EDIT;
    }

    private WalletDetailsParcelableState initOrRestoreState(Bundle savedInstanceState) {
        WalletDetailsParcelableState restoredState = tryRestore(savedInstanceState);
        return restoredState != null
                ? restoredState
                : initState();
    }

    private WalletDetailsParcelableState tryRestore(Bundle savedInstanceState) {
        return savedInstanceState != null
                ? (WalletDetailsParcelableState) savedInstanceState.getParcelable(WALLET_DETAILS_STATE)
                : null;
    }

    private WalletDetailsParcelableState initState() {
        Long cashFlowId = getArguments() != null
                ? getArguments().getLong(WALLET_ID)
                : 0;

        return cashFlowId != 0
                ? getStateFromWallet(localWalletService.findById(cashFlowId))
                : new WalletDetailsParcelableState();
    }

    private WalletDetailsParcelableState getStateFromWallet(Wallet wallet) {
        WalletDetailsParcelableState state = new WalletDetailsParcelableState();
        state.setId(wallet.getId());
        state.setName(wallet.getName());
        state.setInitialAmount(wallet.getInitialAmount());
        state.setCurrentAmount(wallet.getCurrentAmount());
        return state;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.wallet_details, container, false);
        ButterKnife.inject(this, view);
        fillViewsWithData();
        walletName.addTextChangedListener(new WalletNameTextWatcher());
        if (type == DetailsType.ADD) {
            walletInitialAmount.addTextChangedListener(new InitialAmountTextWatcherWhileAdding());
        } else {
            walletInitialAmount.addTextChangedListener(new InitialAmountTextWatcherWhileEditing());
        }
        return view;
    }

    private void fillViewsWithData() {
        if (type == DetailsType.EDIT) {
            walletNameLabel.setVisibility(View.VISIBLE);
            walletInitialAmountLabel.setVisibility(View.VISIBLE);
            walletCurrentAmountLabel.setVisibility(View.VISIBLE);
            walletCurrentAmount.setVisibility(View.VISIBLE);

            //TODO: czy to się zachowuje
            walletCurrentAmount.setTypeface(walletCurrentAmount.getTypeface(), Typeface.BOLD);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(WALLET_DETAILS_STATE, walletDetailsState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            selectedOptionSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectedOptionSave() {
        if (isAnyErrorOccurs()) {
            Wallet wallet = getWalletFromState();
            wallet.setType(Wallet.Type.MY_WALLET);
            if (type == DetailsType.ADD) {
                localWalletService.insert(wallet);
            } else if (type == DetailsType.EDIT) {
                localWalletService.update(wallet);
            }
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean isAnyErrorOccurs() {
        return walletName.getError() != null || walletInitialAmount.getError() != null;
    }

    private Wallet getWalletFromState() {
        Wallet wallet = new Wallet();
        wallet.setId(walletDetailsState.getId());
        wallet.setName(walletDetailsState.getName());
        wallet.setInitialAmount(walletDetailsState.getInitialAmount());
        return wallet;
    }

    private class WalletNameTextWatcher extends EmptyTextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            if (Strings.isNullOrEmpty(s.toString())) {
                if (walletNameLabel.getVisibility() == View.VISIBLE) {
                    walletName.setError(getString(R.string.walletNameIsRequired));
                }
            } else {
                walletNameLabel.setVisibility(View.VISIBLE);
                walletName.setError(null);
            }
            walletDetailsState.setName(s.toString());
        }
    }

    private class InitialAmountTextWatcherWhileAdding extends EmptyTextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            if (Strings.isNullOrEmpty(s.toString())) {
                if (walletInitialAmountLabel.getVisibility() == View.VISIBLE) {
                    walletInitialAmount.setError(getString(R.string.walletInitialAmountIsRequired));
                }
            } else {
                walletInitialAmountLabel.setVisibility(View.VISIBLE);
            }
            validateIfInitialAmountIsNotEmpty();
            Double newInitialAmount = validateIfInitialAmountIsDigit();
            walletDetailsState.setInitialAmount(newInitialAmount);
        }
    }

    private class InitialAmountTextWatcherWhileEditing extends EmptyTextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            validateIfInitialAmountIsNotEmpty();
            Double newInitialAmount = validateIfInitialAmountIsDigit();
            if (walletInitialAmount.getError() == null) {
                double newCurrentAmount = walletDetailsState.getCurrentAmount() + newInitialAmount - walletDetailsState.getInitialAmount();
                walletCurrentAmount.setText(NumberFormat.getCurrencyInstance().format(newCurrentAmount));
                walletDetailsState.setCurrentAmount(newCurrentAmount);
            }
            walletDetailsState.setInitialAmount(newInitialAmount);
        }
    }

    private void validateIfInitialAmountIsNotEmpty() {
        if (Strings.isNullOrEmpty(walletInitialAmount.getText().toString())) {
            walletInitialAmount.setError(getString(R.string.walletInitialAmountIsRequired));
        } else if (getString(R.string.walletInitialAmountIsRequired).equals(walletInitialAmount.getError())) {
            walletInitialAmount.setError(null);
        }
    }

    private Double validateIfInitialAmountIsDigit() {
        Double result = null;
        if (walletInitialAmount.getError() == null || getString(R.string.walletInitialAmountIsNotADigit).equals(walletInitialAmount.getError())) {
            try {
                result = Double.parseDouble(walletInitialAmount.getText().toString());
                walletInitialAmount.setError(null);
            } catch (NumberFormatException e) {
                walletCurrentAmount.setText(NumberFormat.getCurrencyInstance().format(0));
                walletInitialAmount.setError(getString(R.string.walletInitialAmountIsNotADigit));
            }
        }
        return result;
    }

    private static class EmptyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }
}
