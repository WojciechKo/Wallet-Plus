package info.korzeniowski.walletplus.ui.wallet;

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
import butterknife.OnTextChanged;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.WalletNameAndTypeMustBeUniqueException;

public class WalletDetailsFragment extends Fragment {
    public static final String TAG = "walletDetails";

    private enum DetailsType {ADD, EDIT}

    static final String WALLET_ID = "WALLET_ID";

    @InjectView(R.id.walletName)
    TextView walletName;

    @InjectView(R.id.walletInitialAmount)
    TextView walletInitialAmount;

    @Inject @Named("local")
    WalletService localWalletService;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @Inject @Named("amount")
    NumberFormat amountFormat;

    private DetailsType type;
    private Wallet.Builder walletBuilder;
    private String originalName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        init();
    }

    private void init() {
        Long walletId = getArguments().getLong(WALLET_ID);
        type = walletId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        walletBuilder = new Wallet.Builder(getWallet(walletId));
    }

    private Wallet getWallet(Long walletId) {
        if (type == DetailsType.EDIT) {
            Wallet wallet = localWalletService.findById(walletId);
            originalName = wallet.getName();
            return wallet;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet_details, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    private void setupViews() {
        fillViewsWithData();
    }

    private void fillViewsWithData() {
        if (type == DetailsType.EDIT) {
            walletName.setText(walletBuilder.getName());
            walletInitialAmount.setText(amountFormat.format(walletBuilder.getInitialAmount()));
        }
    }

    @OnTextChanged(value = R.id.walletName, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    void walletNameBeforeTextChanged() {
        walletName.setError(null);
    }

    @OnTextChanged(value = R.id.walletName, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void walletNameTextChanged() {
        validateIfEmpty(walletName, getString(R.string.walletNameCantBeEmpty));
        validateIfNameIsUnique();
    }

    private void validateIfNameIsUnique() {
        Wallet found = localWalletService.findByNameAndType(walletName.getText().toString(), Wallet.Type.MY_WALLET);
        if (found != null && !found.getName().equals(originalName)) {
            walletName.setError(getString(R.string.walletNameHaveToBeUnique));
        }
    }

    @OnTextChanged(value = R.id.walletInitialAmount, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    void walletInitialAmountBeforeTextChanged() {
        walletInitialAmount.setError(null);
    }

    @OnTextChanged(value = R.id.walletInitialAmount, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void walletInitialAmountTextChanged() {
        validateIfEmpty(walletInitialAmount, getString(R.string.walletInitialAmountCantBeEmpty));
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
            walletBuilder.setType(Wallet.Type.MY_WALLET);
            try {
                if (type == DetailsType.ADD) {
                    localWalletService.insert(walletBuilder.build());
                } else if (type == DetailsType.EDIT) {
                    localWalletService.update(walletBuilder.build());
                }
                getActivity().getSupportFragmentManager().popBackStack();
            } catch (WalletNameAndTypeMustBeUniqueException e) {
                walletName.setError(getString(R.string.walletNameHaveToBeUnique));
            }
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
        walletBuilder.setName(walletName.getText().toString());
        walletBuilder.setInitialAmount(Double.parseDouble(walletInitialAmount.getText().toString()));
        if (type == DetailsType.ADD) {
            walletBuilder.setCurrentAmount(walletBuilder.getInitialAmount());
        } else if (type == DetailsType.EDIT) {
            walletBuilder.setCurrentAmount(walletBuilder.getCurrentAmount() + walletBuilder.getInitialAmount() - walletBuilder.getInitialAmount());
        }
    }

    private void selectedOptionDelete() {
        showConfirmationAlert();
    }

    private void showConfirmationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(getConfirmationMessage())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryDelete(walletBuilder.getId());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private String getConfirmationMessage() {
        int count = (int) localCashFlowService.countAssignedToWallet(walletBuilder.getId());
        String msg = getActivity().getString(R.string.walletDeleteConfirmation);
        return MessageFormat.format(msg, count);
    }

    private void tryDelete(Long id) {
        localWalletService.deleteById(id);
    }
}
