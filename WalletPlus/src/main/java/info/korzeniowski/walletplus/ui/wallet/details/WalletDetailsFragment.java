package info.korzeniowski.walletplus.ui.wallet.details;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.text.MessageFormat;
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

    @InjectView(R.id.walletName)
    TextView walletName;

    @InjectView(R.id.walletInitialAmount)
    TextView walletInitialAmount;

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
        initState();
    }

    private void initState() {
        wallet = localWalletService.findById(getArguments().getLong(WALLET_ID));
        if (wallet == null) {
            wallet = new Wallet();
            type = DetailsType.ADD;
        } else {
            type = DetailsType.EDIT;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet_details, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    private void setupViews() {
        fillViewsWithData();
    }

    private void fillViewsWithData() {
        if (type == DetailsType.EDIT) {
            walletName.setText(wallet.getName());
            walletInitialAmount.setText(amountFormat.format(wallet.getInitialAmount()));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_delete, menu);
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            selectedOptionSave();
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            selectedOptionDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectedOptionSave() {
        if (validateIfNoEmptyFields()) {
            getDataFromViews();
            wallet.setType(Wallet.Type.MY_WALLET);
            if (type == DetailsType.ADD) {
                localWalletService.insert(wallet);
            } else if (type == DetailsType.EDIT) {
                localWalletService.update(wallet);
            }
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean validateIfNoEmptyFields() {
        validateIfEmpty(walletName, getString(R.string.walletNameCantBeEmpty));
        validateIfEmpty(walletInitialAmount, getString(R.string.walletInitialAmountCantBeEmpty));
        return !isAnyErrorsAppear();
    }

    private void validateIfEmpty(TextView textView, String errorMsg) {
        if (Strings.isNullOrEmpty(textView.getText().toString())) {
            textView.setError(errorMsg);
        }
    }

    private boolean isAnyErrorsAppear() {
        return walletName.getError() != null || walletInitialAmount.getError() != null;
    }

    private void getDataFromViews() {
        wallet.setName(walletName.getText().toString());
        wallet.setInitialAmount(Double.parseDouble(walletInitialAmount.getText().toString()));
        if (type == DetailsType.ADD) {
            wallet.setCurrentAmount(wallet.getInitialAmount());
        } else if (type == DetailsType.EDIT) {
            wallet.setCurrentAmount(wallet.getCurrentAmount() + wallet.getInitialAmount() - wallet.getInitialAmount());
        }
    }

    private void selectedOptionDelete() {
        showDeleteConfirmationAlert();
    }

    private void showDeleteConfirmationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(getConfirmationMessage())
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryDelete(wallet.getId());
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private String getConfirmationMessage() {
        int count = (int) localCashFlowService.countAssignedToWallet(wallet.getId());
        String msg = getActivity().getString(R.string.walletDeleteConfirmation);
        return MessageFormat.format(msg, count);
    }

    private void tryDelete(Long id) {
        localWalletService.deleteById(id);
    }
}
