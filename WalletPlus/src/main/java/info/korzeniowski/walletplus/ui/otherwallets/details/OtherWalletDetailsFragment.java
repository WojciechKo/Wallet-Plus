package info.korzeniowski.walletplus.ui.otherwallets.details;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import java.text.NumberFormat;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;

public class OtherWalletDetailsFragment extends Fragment {
    public static final String TAG = "walletDetails";
    public static final String ARGUMENT_WALLET_ID = "WALLET_ID";

    @InjectView(R.id.walletNameLabel)
    TextView walletNameLabel;

    @InjectView(R.id.walletName)
    EditText walletName;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Inject
    @Named("amount")
    NumberFormat amountFormat;

    private Long walletId;
    private DetailsAction detailsAction;
    private Optional<Wallet> walletToEdit;

    public static OtherWalletDetailsFragment newInstance(Long walletId) {
        OtherWalletDetailsFragment fragment = new OtherWalletDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(ARGUMENT_WALLET_ID, walletId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ((WalletPlus) getActivity().getApplication()).inject(this);

        walletId = getArguments() == null ? -1 : getArguments().getLong(ARGUMENT_WALLET_ID);

        if (walletId == -1) {
            detailsAction = DetailsAction.ADD;
            walletToEdit = Optional.absent();
        } else {
            detailsAction = DetailsAction.EDIT;
            walletToEdit = Optional.of(localWalletService.findById(walletId));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_other_wallet_details, container, false);
        ButterKnife.inject(this, view);

        if (detailsAction == DetailsAction.EDIT) {
            walletName.setText(walletToEdit.get().getName());
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_save) {
            onSaveOptionSelected();
            return true;
        }
        return result;
    }

    private void onSaveOptionSelected() {
        if (Strings.isNullOrEmpty(walletName.getText().toString())) {
            walletName.setError(getString(R.string.walletNameIsRequired));
        } else {
            walletName.setError(null);
        }

        if (walletName.getError() == null) {
            Wallet wallet = new Wallet();
            wallet.setName(walletName.getText().toString());
            wallet.setType(Wallet.Type.OTHER);

            if (detailsAction == DetailsAction.ADD) {
                localWalletService.insert(wallet);
                getActivity().setResult(Activity.RESULT_OK);
            } else if (detailsAction == DetailsAction.EDIT) {
                wallet.setId(walletId);
                localWalletService.update(wallet);
                getActivity().setResult(Activity.RESULT_OK);
            }
            getActivity().finish();
        }
    }

    @OnTextChanged(value = R.id.walletName, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterWalletNameChanged(Editable s) {
        if (Strings.isNullOrEmpty(s.toString())) {
            if (walletNameLabel.getVisibility() == View.VISIBLE) {
                walletName.setError(getString(R.string.walletNameIsRequired));
            }
        } else {
            walletName.setError(null);
        }

        walletNameLabel.setVisibility(View.VISIBLE);
    }

    private enum DetailsAction {ADD, EDIT}
}
