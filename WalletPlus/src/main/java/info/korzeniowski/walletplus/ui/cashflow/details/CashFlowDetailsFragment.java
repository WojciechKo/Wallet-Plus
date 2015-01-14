package info.korzeniowski.walletplus.ui.cashflow.details;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import info.hoang8f.widget.FButton;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

public class CashFlowDetailsFragment extends Fragment {
    public static final String TAG = "CashFlowDetailsFragment";
    public static final String ARGUMENT_CASH_FLOW_ID = "CASH_FLOW_ID";
    private static final String CASH_FLOW_DETAILS_STATE = "cashFlowDetailsState";
    @InjectView(R.id.fromWallet)
    Spinner fromWallet;

    @InjectView(R.id.toWallet)
    Spinner toWallet;

    @InjectView(R.id.typeToggle)
    FButton typeToggle;

    @InjectView(R.id.transferToggle)
    FButton transferToggle;

    @InjectView(R.id.amountLabel)
    TextView amountLabel;

    @InjectView(R.id.amount)
    EditText amount;

    @InjectView(R.id.commentLabel)
    TextView commentLabel;

    @InjectView(R.id.comment)
    EditText comment;

    @InjectView(R.id.category)
    Button category;

    @InjectView(R.id.categoryLabel)
    TextView categoryLabel;

    @InjectView(R.id.removeCategory)
    ImageButton removeCategory;

    @InjectView(R.id.datePicker)
    Button datePicker;

    @InjectView(R.id.timePicker)
    Button timePicker;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private List<Wallet> myWallets;

    private List<Wallet> otherWallets;

    private List<Category> categoryList;

    private CashFlowDetailsParcelableState cashFlowDetailsState;

    private DetailsAction detailsAction;

    public static CashFlowDetailsFragment newInstance(Long cashFlowId) {
        CashFlowDetailsFragment fragment = new CashFlowDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(ARGUMENT_CASH_FLOW_ID, cashFlowId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ((WalletPlus) getActivity().getApplication()).inject(this);

        Long cashFlowId = getArguments() == null ? -1 : getArguments().getLong(ARGUMENT_CASH_FLOW_ID);
        if (cashFlowId == -1) {
            detailsAction = DetailsAction.ADD;
        } else {
            detailsAction = DetailsAction.EDIT;
        }

        CashFlowDetailsParcelableState restored = null;
        if (savedInstanceState != null) {
            restored = savedInstanceState.getParcelable(CASH_FLOW_DETAILS_STATE);
        }
        cashFlowDetailsState = MoreObjects.firstNonNull(restored, initCashFlowDetailsState(cashFlowId));

        categoryList = localCategoryService.getMainCategories();
        myWallets = localWalletService.getMyWallets();
        otherWallets = localWalletService.getContractors();
    }

    private CashFlowDetailsParcelableState initCashFlowDetailsState(Long cashFlowId) {
        if (detailsAction == DetailsAction.ADD) {
            return new CashFlowDetailsParcelableState();
        } else if (detailsAction == DetailsAction.EDIT) {
            return new CashFlowDetailsParcelableState(localCashFlowService.findById(cashFlowId));
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.cash_flow_details_fragment, container, false);
        ButterKnife.inject(this, view);

        setupTypeDependentViews();
        if (detailsAction == DetailsAction.EDIT) {
            amount.setText(Strings.nullToEmpty(cashFlowDetailsState.getAmount()));
            comment.setText(cashFlowDetailsState.getComment());
        }
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CASH_FLOW_DETAILS_STATE, cashFlowDetailsState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * ***********
     * LISTENERS *
     * ***********
     */
    @OnClick(R.id.typeToggle)
    void onTypeToggleClicked() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.setType(cashFlowDetailsState.getPreviousType());
        } else {
            cashFlowDetailsState.swapWallets();
        }
        setupTypeDependentViews();
    }

    @OnClick(R.id.transferToggle)
    void onTransferToggleClicked() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.swapWallets();
        } else {
            cashFlowDetailsState.setType(CashFlow.Type.TRANSFER);
        }
        setupTypeDependentViews();
    }

    @OnItemSelected(R.id.fromWallet)
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        cashFlowDetailsState.setFromWallet(selected);
    }

    @OnItemSelected(R.id.toWallet)
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        cashFlowDetailsState.setToWallet(selected);
    }

    @OnTextChanged(value = R.id.amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAmountChanged(Editable s) {
        cashFlowDetailsState.setAmount(s.toString());

        if (Strings.isNullOrEmpty(cashFlowDetailsState.getAmount())) {
            if (amountLabel.getVisibility() == View.VISIBLE) {
                amount.setError("Amount can't be empty.");
            }
        } else if (!cashFlowDetailsState.isAmountValid()) {
            amount.setError("Write amount in this pattern: (+/-)149.1234");
        }

        amountLabel.setVisibility(View.VISIBLE);
    }

    @OnTextChanged(value = R.id.comment, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onCommentChanged(Editable s) {
        cashFlowDetailsState.setComment(s.toString());

        commentLabel.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.category)
    void onCategoryClick() {
        ExpandableListView expandableListView = (ExpandableListView) View.inflate(getActivity(), R.layout.category_list, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cashflowCategoryChooseAlertTitle))
                .setView(expandableListView)
                .create();

        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList, new OnContentClickListener<Category>() {
            @Override
            public void onContentClick(Category content) {
                cashFlowDetailsState.setCategory(content);
                category.setText(getCategoryText(cashFlowDetailsState.getCategory()));
                alertDialog.dismiss();
            }
        }));

        alertDialog.show();
    }

    @OnClick(R.id.removeCategory)
    void onRemoveCategoryClick() {
        cashFlowDetailsState.setCategory(null);
        category.setText(getCategoryText(cashFlowDetailsState.getCategory()));
    }

    @OnClick(R.id.datePicker)
    public void onDatePickerClick() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cashFlowDetailsState.getDate());
        new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        cashFlowDetailsState.setDate(calendar.getTimeInMillis());
                        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @OnClick(R.id.timePicker)
    public void onTimePickerClick() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cashFlowDetailsState.getDate());

        new TimePickerDialog(
                getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        cashFlowDetailsState.setDate(calendar.getTimeInMillis());
                        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity())
        ).show();
    }

    private void setupTypeDependentViews() {
        setupToggles();
        setupWallets();
        setupCategory();
    }

    private void setupToggles() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            typeToggle.setText(getString(R.string.income));
            typeToggle.setTextColor(getResources().getColor(R.color.white));
            typeToggle.setButtonColor(getResources().getColor(R.color.green));

            transferToggle.setTextColor(getResources().getColor(R.color.black));
            transferToggle.setButtonColor(getResources().getColor(R.color.whiteE5));

        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            typeToggle.setText(getString(R.string.expanse));
            typeToggle.setTextColor(getResources().getColor(R.color.white));
            typeToggle.setButtonColor(getResources().getColor(R.color.red));

            transferToggle.setTextColor(getResources().getColor(R.color.black));
            transferToggle.setButtonColor(getResources().getColor(R.color.whiteE5));

        } else if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            typeToggle.setText(getPreviousTypeName());
            typeToggle.setTextColor(getResources().getColor(R.color.black));
            typeToggle.setButtonColor(getResources().getColor(R.color.whiteE5));

            transferToggle.setTextColor(getResources().getColor(R.color.white));
            transferToggle.setButtonColor(getResources().getColor(R.color.blue));
        }
    }

    private String getPreviousTypeName() {
        if (cashFlowDetailsState.getPreviousType() == CashFlow.Type.INCOME) {
            return getString(R.string.income);
        } else if (cashFlowDetailsState.getPreviousType() == CashFlow.Type.EXPANSE) {
            return getString(R.string.expanse);
        }
        return "";
    }

    private void setupWallets() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            setupFromWallet(otherWallets);
            setupToWallet(myWallets);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            setupFromWallet(myWallets);
            setupToWallet(otherWallets);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            setupFromWallet(myWallets);
            setupToWallet(myWallets);
        }
    }

    private void setupFromWallet(List<Wallet> wallets) {
        fromWallet.setAdapter(new WalletAdapter(getActivity(), wallets));
        fromWallet.setSelection(wallets.indexOf(cashFlowDetailsState.getFromWallet()));
        ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
    }

    private void setupToWallet(List<Wallet> wallets) {
        toWallet.setAdapter(new WalletAdapter(getActivity(), wallets));
        toWallet.setSelection(wallets.indexOf(cashFlowDetailsState.getToWallet()));
        ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
    }

    private void setupCategory() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            category.setVisibility(View.GONE);
            categoryLabel.setVisibility(View.GONE);
            removeCategory.setVisibility(View.GONE);
        } else {
            category.setVisibility(View.VISIBLE);
            categoryLabel.setVisibility(View.VISIBLE);
            removeCategory.setVisibility(View.VISIBLE);
            category.setText(getCategoryText(cashFlowDetailsState.getCategory()));
        }
    }

    private String getCategoryText(Category category) {
        if (category == null) {
            return getString(R.string.categoryNoCategoryName);
        } else if (category.getParent() == null) {
            return category.getName();
        }
        return category.getName() + " (" + category.getParent().getName() + ")";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            onSaveOptionSelected();
            return true;
        }
        return false;
    }

    private void onSaveOptionSelected() {
        if (Strings.isNullOrEmpty(cashFlowDetailsState.getAmount())) {
            amount.setError("Amount can't be empty.");
        } else if (!cashFlowDetailsState.isAmountValid()) {
            amount.setError("Write amount in this pattern: [+|-] 149.1234");
        }

        boolean isValid = true;
        if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            isValid = validateIfFromWalletIsDifferentThanToWallet();
        }

        if (isValid && amount.getError() == null) {
            if (DetailsAction.ADD.equals(detailsAction)) {
                localCashFlowService.insert(cashFlowDetailsState.buildCashFlow());
            } else if (DetailsAction.EDIT.equals(detailsAction)) {
                localCashFlowService.update(cashFlowDetailsState.buildCashFlow());
            }
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

    private boolean validateIfFromWalletIsDifferentThanToWallet() {
        if (cashFlowDetailsState.getFromWallet() != null
                && cashFlowDetailsState.getFromWallet().equals(cashFlowDetailsState.getToWallet())) {
            Toast.makeText(getActivity(), getResources().getString(R.string.cashFlowFromAndToWalletsNeedToBeDifferent), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private enum DetailsAction {ADD, EDIT}

    class WalletAdapter extends BaseAdapter {
        final List<Wallet> wallets;
        final WeakReference<Context> context;

        private WalletAdapter(Context context, List<Wallet> list) {
            this.context = new WeakReference<>(context);
            wallets = list;
        }

        @Override
        public int getCount() {
            return wallets.size();
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(context.get());
                textView.setTextSize(context.get().getResources().getDimension(R.dimen.verySmallFontSize));
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(getItem(position).getName());
            return textView;
        }

        @Override
        public Wallet getItem(int position) {
            return wallets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
