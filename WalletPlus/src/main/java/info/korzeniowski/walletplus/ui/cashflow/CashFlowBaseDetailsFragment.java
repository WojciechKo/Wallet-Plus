package info.korzeniowski.walletplus.ui.cashflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.text.NumberFormat;
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
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.ui.category.CategoryExpandableListAdapter;
import info.korzeniowski.walletplus.widget.OnContentClickListener;


public class CashFlowBaseDetailsFragment extends Fragment implements OnCashFlowDetailsChangedListener {
    public static final String CASH_FLOW_DETAILS_STATE = "cashFlowDetailsState";

    private enum DetailsMode {ADD, EDIT}

    @InjectView(R.id.fromWallet)
    Spinner fromWallet;

    @InjectView(R.id.toWallet)
    Spinner toWallet;

    @InjectView(R.id.amount)
    EditText amount;

    @InjectView(R.id.category)
    Button category;

    @InjectView(R.id.removeCategory)
    ImageButton removeCategory;

    @InjectView(R.id.comment)
    EditText comment;

    @InjectView(R.id.datePicker)
    Button datePicker;

    @InjectView(R.id.timePicker)
    Button timePicker;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @Inject @Named("local")
    WalletService localWalletService;

    @Inject @Named("local")
    CategoryService localCategoryService;

    @Inject @Named("amount")
    NumberFormat amountFormat;

    private CashFlow.Type cashFlowType;
    private DetailsMode detailsMode;
    protected List<Wallet> fromWalletList;
    protected List<Wallet> toWalletList;
    private List<Category> categoryList;
    private CashFlowDetailsParcelableState cashFlowDetailsState;
    private OnCashFlowDetailsChangedListener onCashFlowDetailsChangedListener;

    public void setCashFlowType(CashFlow.Type cashFlowType) {
        this.cashFlowType = cashFlowType;
    }

    public CashFlow.Type getCashFlowType() {
        return cashFlowType;
    }

    public static CashFlowBaseDetailsFragment newInstance(CashFlow.Type type, Parcelable state) {
        CashFlowBaseDetailsFragment fragment = new CashFlowBaseDetailsFragment();
        fragment.setCashFlowType(type);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CASH_FLOW_DETAILS_STATE, state);
        fragment.setArguments(bundle);
        return fragment;
    }

    /////////////////////////////////////////
    // change handlers
    @Override
    public void onAmountChanged() {
        String amountString = getAmountStringFromState();
        if (!amount.getText().toString().equals(amountString)) {
            amount.setText(amountString);
        }
    }

    private String getAmountStringFromState() {
        Float amount = cashFlowDetailsState.getAmount();
        if (amount == null) {
            return "";
        }
        return amountFormat.format(amount);
    }

    @Override
    public void onCommentChanged() {
        String commentString = cashFlowDetailsState.getComment();
        if (!comment.getText().toString().equals(commentString)) {
            comment.setText(commentString);
        }
    }

    @Override
    public void onCategoryChanged() {
        category.setText(getCategoryText(getCategoryFromState()));
    }

    @Override
    public void onFromWalletChanged() {
        if (fromWallet != null) {
            fromWallet.setSelection(fromWalletList.indexOf(getFromWalletFromState()));
            ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
        }
    }

    private Wallet getFromWalletFromState() {
        if (cashFlowType == CashFlow.Type.INCOME) {
            return cashFlowDetailsState.getIncomeFromWallet();
        } else if (cashFlowType == CashFlow.Type.EXPANSE || cashFlowType == CashFlow.Type.TRANSFER) {
            return cashFlowDetailsState.getExpanseFromWallet();
        }
        return null;
    }

    @Override
    public void onToWalletChanged() {
        if (toWallet != null) {
            toWallet.setSelection(toWalletList.indexOf(getToWalletFromState()));
            ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
        }
    }

    private Wallet getToWalletFromState() {
        if (cashFlowType == CashFlow.Type.INCOME || cashFlowType == CashFlow.Type.TRANSFER) {
            return cashFlowDetailsState.getIncomeToWallet();
        } else if (cashFlowType == CashFlow.Type.EXPANSE) {
            return cashFlowDetailsState.getExpanseToWallet();
        }
        return null;
    }

    @Override
    public void onDateChanged() {
        setDatePickerButtonTextFromState();
    }

    @Override
    public void onTimeChanged() {
        setTimePickerButtonTextFromState();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onCashFlowDetailsChangedListener = (OnCashFlowDetailsChangedListener) activity;
        ((MainActivity) activity).addOnCashFlowDetailsChangedListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        cashFlowDetailsState = getArguments().getParcelable(CASH_FLOW_DETAILS_STATE);
        initState(cashFlowDetailsState);
        setHasOptionsMenu(true);
    }

    private void initState(CashFlowDetailsParcelableState cashFlowDetailsState) {
        if (cashFlowDetailsState.isInit()) {
            return;
        } else if (cashFlowDetailsState.getId() == 0) {
            cashFlowDetailsState.setDate(Calendar.getInstance().getTimeInMillis());
            cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
            cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
        } else {
            CashFlow cashFlow = localCashFlowService.findById(cashFlowDetailsState.getId());
            cashFlowDetailsState.setAmount(cashFlow.getAmount());
            cashFlowDetailsState.setComment(cashFlow.getComment());
            cashFlowDetailsState.setDate(cashFlow.getDateTime().getTime());

            if (cashFlow.isIncome()) {
                cashFlowDetailsState.setIncomeCategory(cashFlow.getCategory());
                cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
                cashFlowDetailsState.setIncomeFromWallet(cashFlow.getFromWallet());
                cashFlowDetailsState.setIncomeToWallet(cashFlow.getToWallet());
            } else if (cashFlow.isExpanse()) {
                cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
                cashFlowDetailsState.setExpanseCategory(cashFlow.getCategory());
                cashFlowDetailsState.setExpanseFromWallet(cashFlow.getFromWallet());
                cashFlowDetailsState.setExpanseToWallet(cashFlow.getToWallet());
            } else if (cashFlow.isTransfer()) {
                cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
                cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
                cashFlowDetailsState.setExpanseFromWallet(cashFlow.getFromWallet());
                cashFlowDetailsState.setIncomeToWallet(cashFlow.getToWallet());
            }
        }
        cashFlowDetailsState.setInit(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.cashflow_details_fragment, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    public void setupViews() {
        initFields();
        setupAdapters();
        fillViewsWithData();
    }

    protected void initFields() {
        detailsMode = cashFlowDetailsState.getId() == 0L ? DetailsMode.ADD : DetailsMode.EDIT;
        fromWalletList = Lists.newArrayList();
        toWalletList = Lists.newArrayList();
        categoryList = Lists.newArrayList();
    }

    private void setupAdapters() {
        toWallet.setAdapter(new WalletAdapter(getActivity(), toWalletList));
        fromWallet.setAdapter(new WalletAdapter(getActivity(), fromWalletList));
    }

    private void fillViewsWithData() {
        fillWalletLists();
        fillCategoryList();
        amount.setText(getAmountStringFromState());
        comment.setText(cashFlowDetailsState.getComment());
        category.setText(getCategoryText(getCategoryFromState()));
        fromWallet.setSelection(fromWalletList.indexOf(getFromWalletFromState()));
        ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
        toWallet.setSelection(toWalletList.indexOf(getToWalletFromState()));
        ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
        setDatePickerButtonTextFromState();
        setTimePickerButtonTextFromState();
        if (cashFlowType == CashFlow.Type.TRANSFER) {
            category.setVisibility(View.GONE);
        }
    }

    private String getCategoryText(Category category) {
        if (category.getParent() == null) {
            return category.getName();
        }
        return category.getName() + " (" + category.getParent().getName() + ")";
    }

    private Category getCategoryFromState() {
        if (cashFlowType == CashFlow.Type.INCOME) {
            return cashFlowDetailsState.getIncomeCategory();
        } else if (cashFlowType == CashFlow.Type.EXPANSE) {
            return cashFlowDetailsState.getExpanseCategory();
        } else if (cashFlowType == CashFlow.Type.TRANSFER) {
            return localCashFlowService.getTransferCategory();
        }
        return localCashFlowService.getOtherCategory();
    }

    protected void fillWalletLists() {
        if (cashFlowType == CashFlow.Type.INCOME) {
            fromWalletList.addAll(localWalletService.getContractors());
            toWalletList.addAll(localWalletService.getMyWallets());
        } else if (cashFlowType == CashFlow.Type.EXPANSE) {
            fromWalletList.addAll(localWalletService.getMyWallets());
            toWalletList.addAll(localWalletService.getContractors());
        } else if (cashFlowType == CashFlow.Type.TRANSFER) {
            fromWalletList.addAll(localWalletService.getMyWallets());
            toWalletList.addAll(localWalletService.getMyWallets());
        }
    }

    private void fillCategoryList() {
        if (cashFlowType == CashFlow.Type.INCOME) {
            categoryList.addAll(localCategoryService.getMainIncomeTypeCategories());
        } else if (cashFlowType == CashFlow.Type.EXPANSE) {
            categoryList.addAll(localCategoryService.getMainExpenseTypeCategories());
        }
    }

    @Override
    public void onDestroy() {
        ((MainActivity) getActivity()).removeOnCashFlowDetailsChangedListeners(this);
        super.onDestroy();
    }

    @OnItemSelected(R.id.fromWallet)
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        if (cashFlowType == CashFlow.Type.INCOME) {
            cashFlowDetailsState.setIncomeFromWallet(selected);
        } else if (cashFlowType == CashFlow.Type.EXPANSE || cashFlowType == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.setExpanseFromWallet(selected);
        }
        onCashFlowDetailsChangedListener.onFromWalletChanged();
    }

    @OnItemSelected(R.id.toWallet)
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        if (cashFlowType == CashFlow.Type.INCOME || cashFlowType == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.setIncomeToWallet(selected);
        } else if (cashFlowType == CashFlow.Type.EXPANSE) {
            cashFlowDetailsState.setExpanseToWallet(selected);
        }
        onCashFlowDetailsChangedListener.onToWalletChanged();
    }

    @OnTextChanged(value = R.id.amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onAmountAfterTextChanged(Editable s) {
        if (isDecimal(s)) {
            int numberOfDigitsToDelete = getNumberOfDigitsToDelete(s);
            s.delete(s.length() - numberOfDigitsToDelete, s.length());
        }
        if (Strings.isNullOrEmpty(s.toString())) {
            cashFlowDetailsState.setAmount(null);
        } else {
            cashFlowDetailsState.setAmount(Float.parseFloat(s.toString()));
        }
        onCashFlowDetailsChangedListener.onAmountChanged();
    }

    private boolean isDecimal(Editable s) {
        return s.toString().contains(".");
    }

    private int getNumberOfDigitsToDelete(Editable s) {
        int allowedNumberOfDigitsAfterComa = 2;
        int indexOfComa = s.toString().indexOf('.');
        if (indexOfComa < s.length() - 1 - allowedNumberOfDigitsAfterComa) {
            return s.length() - indexOfComa - 1 - allowedNumberOfDigitsAfterComa;
        }
        return 0;
    }

    @OnTextChanged(value = R.id.comment, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onCommentAfterTextChanged(Editable s) {
        cashFlowDetailsState.setComment(s.toString());
        onCashFlowDetailsChangedListener.onCommentChanged();
    }

    @OnClick(R.id.category)
    public void onClickCategory() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_list, null);
        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.superList);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cashflowCategoryChooseAlertTitle))
                .setView(expandableListView)
                .create();

        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList, new OnContentClickListener() {
            @Override
            public void onContentClick(Long id) {
                Category category = localCategoryService.findById(id);
                if (cashFlowType == CashFlow.Type.INCOME) {
                    cashFlowDetailsState.setIncomeCategory(category);
                } else if (cashFlowType == CashFlow.Type.EXPANSE) {
                    cashFlowDetailsState.setExpanseCategory(category);
                }
                onCashFlowDetailsChangedListener.onCategoryChanged();
                alertDialog.dismiss();
            }
        }));

        alertDialog.show();
    }

    @OnClick(R.id.removeCategory)
    public void onClickRemoveCategory() {
        if (cashFlowType == CashFlow.Type.INCOME) {
            cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
        } else if (cashFlowType == CashFlow.Type.EXPANSE) {
            cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
        }
        onCashFlowDetailsChangedListener.onCategoryChanged();
    }

    @OnClick(R.id.datePicker)
    public void onClickDatePicker() {
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
                        onCashFlowDetailsChangedListener.onDateChanged();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @OnClick(R.id.timePicker)
    public void onClickTimePicker() {
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
                        onCashFlowDetailsChangedListener.onTimeChanged();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity())
        ).show();
    }

    private void setDatePickerButtonTextFromState() {
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
    }

    private void setTimePickerButtonTextFromState() {
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == info.korzeniowski.walletplus.R.id.menu_save) {
            actionSave();
            return true;
        }
        return false;
    }

    void actionSave() {
        if (preValidations() && handleActionSave()) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean preValidations() {
        return validateAmount();
    }

    private boolean validateAmount() {
        if (Strings.isNullOrEmpty(amount.getText().toString())) {
            amount.setError("Amount can't be empty.");
            return false;
        }
        return true;
    }

    private boolean handleActionSave() {
        if (DetailsMode.ADD.equals(detailsMode)) {
            return tryInsert();
        } else if (DetailsMode.EDIT.equals(detailsMode)) {
            return tryUpdate();
        }
        return false;
    }

    private boolean tryInsert() {
        CashFlow cashFlow = new CashFlow(cashFlowDetailsState, cashFlowType);
        if (cashFlowType == CashFlow.Type.TRANSFER) {
            cashFlow.setCategory(localCashFlowService.getTransferCategory());
        }
        localCashFlowService.insert(cashFlow);
        return true;
    }

    private boolean tryUpdate() {
        CashFlow cashFlow = new CashFlow(cashFlowDetailsState, cashFlowType);
        if (cashFlowType == CashFlow.Type.TRANSFER) {
            cashFlow.setCategory(localCashFlowService.getTransferCategory());
        }
        localCashFlowService.update(cashFlow);
        return true;
    }

    private class WalletAdapter extends BaseAdapter {
        List<Wallet> wallets;
        Context context;

        private WalletAdapter(Context context, List<Wallet> list) {
            this.context = context;
            wallets = list;
        }

        @Override
        public int getCount() {
            return wallets.size();
        }

        @Override
        public Wallet getItem(int position) {
            return wallets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(context);
                textView.setTextSize(getResources().getDimension(R.dimen.smallFontSize));
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(getItem(position).getName());
            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }
}
