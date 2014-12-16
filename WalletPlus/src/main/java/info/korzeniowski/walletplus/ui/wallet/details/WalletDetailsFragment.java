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

    private DetailsType type;

    private Wallet wallet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            restoreFields(savedInstanceState);
        } else {
            initFields();
        }
    }

    private void restoreFields(Bundle savedInstanceState) {
        type = DetailsType.values()[savedInstanceState.getInt("detailsType")];
        wallet = savedInstanceState.getParcelable("wallet");
    }

    private void initFields() {
        Long walletId = getArguments() != null ? getArguments().getLong(WALLET_ID) : 0;
        if (walletId == 0) {
            type = DetailsType.ADD;
            wallet = new Wallet();
        } else {
            type = DetailsType.EDIT;
            wallet = localWalletService.findById(walletId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.wallet_details, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        setupListeners();
        return view;
    }

    private void setupViews() {
        if (type == DetailsType.EDIT) {
            walletNameLabel.setVisibility(View.VISIBLE);
            walletInitialAmountLabel.setVisibility(View.VISIBLE);
            walletCurrentAmountLabel.setVisibility(View.VISIBLE);
            walletCurrentAmount.setVisibility(View.VISIBLE);

            //TODO: czy to się zachowuje
            walletCurrentAmount.setTypeface(walletCurrentAmount.getTypeface(), Typeface.BOLD);
        }
    }

    private void setupListeners() {
        walletName.addTextChangedListener(new WalletNameTextWatcher());
        if (type == DetailsType.ADD) {
            walletInitialAmount.addTextChangedListener(new InitialAmountTextWatcherWhileAdding());
        } else {
            walletInitialAmount.addTextChangedListener(new InitialAmountTextWatcherWhileEditing());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (wallet.getName() == null) {
            walletName.setText("");
        } else {
            walletName.setText(wallet.getName());
        }
        if (wallet.getInitialAmount() == null) {
            walletInitialAmount.setText("");
        } else {
            walletInitialAmount.setText(amountFormat.format(wallet.getInitialAmount()));
        }

        if (type == DetailsType.EDIT) {
            walletCurrentAmount.setText(NumberFormat.getCurrencyInstance().format(wallet.getCurrentAmount()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("detailsType", type.ordinal());
        outState.putParcelable("wallet", wallet);
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
        if (!isAnyErrorOccurs()) {
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

    private class WalletNameTextWatcher extends EmptyTextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            if (Strings.isNullOrEmpty(s.toString())) {
                if (walletNameLabel.getVisibility() == View.VISIBLE) {
                    walletName.setError(getString(R.string.walletNameIsRequired));
                }
            } else if (walletName.getError() != null &&
                    walletName.getError().toString().equals(getString(R.string.walletNameIsRequired))) {
                walletName.setError(null);
            } else {
                walletNameLabel.setVisibility(View.VISIBLE);
            }
            if (walletName.getError() == null) {
                wallet.setName(s.toString());
            }
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
            if (walletInitialAmountLabel.getVisibility() == View.VISIBLE) {
                validateIfInitialAmountIsNotEmpty();
                Double newInitialAmount = validateIfInitialAmountIsDigit();
                if (newInitialAmount != null) {
                    wallet.setInitialAmount(newInitialAmount);
                }
            }
        }
    }

    private class InitialAmountTextWatcherWhileEditing extends EmptyTextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            validateIfInitialAmountIsNotEmpty();
            Double newInitialAmount = validateIfInitialAmountIsDigit();
            if (newInitialAmount != null) {
                double newCurrentAmount = wallet.getCurrentAmount() + newInitialAmount - wallet.getInitialAmount();
                walletCurrentAmount.setText(NumberFormat.getCurrencyInstance().format(newCurrentAmount));
                wallet.setInitialAmount(newInitialAmount);
                wallet.setCurrentAmount(newCurrentAmount);
            }
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
        Double result = Strings.isNullOrEmpty(walletInitialAmount.getText().toString())
                ? 0.0
                : null;

        if (walletInitialAmount.getError() == null || getString(R.string.walletInitialAmountIsNotADigit).equals(walletInitialAmount.getError())) {
            try {
                result = Double.parseDouble(walletInitialAmount.getText().toString());
                walletInitialAmount.setError(null);
            } catch (NumberFormatException e) {
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
