package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsParcelableState;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsStatusChangedEvent;
import info.korzeniowski.walletplus.ui.category.list.CategoryExpandableListAdapter;
import info.korzeniowski.walletplus.widget.OnContentClickListener;


public abstract class CashFlowBaseDetailsFragment extends Fragment {
    public static final String CASH_FLOW_DETAILS_STATE = "cashFlowDetailsState";
    public static final String IS_SELECTED = "isSelected";
    public static final String digitRegex = "^(\\+|\\-)?(([0-9]+(\\.[0-9]{0,4})?)|(\\.[0-9]{0,4}))$";

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

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    @Inject
    Bus bus;

    protected List<Wallet> fromWalletList;

    protected List<Wallet> toWalletList;
    protected List<Category> categoryList;
    protected CashFlowDetailsParcelableState cashFlowDetailsState;

    private TextWatcher amountTextWatcher;
    private TextWatcher commentTextWatcher;
    private DetailsMode detailsMode;
    private boolean isSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        if (savedInstanceState != null) {
            cashFlowDetailsState = savedInstanceState.getParcelable(CASH_FLOW_DETAILS_STATE);
            isSelected = savedInstanceState.getBoolean(IS_SELECTED);
        } else {
            cashFlowDetailsState = getArguments().getParcelable(CASH_FLOW_DETAILS_STATE);
            isSelected = getArguments().getBoolean(IS_SELECTED);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.cashflow_details_fragment, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    private void setupViews() {
        initFields();
        setupAdapters();
    }

    protected void initFields() {
        detailsMode = cashFlowDetailsState.getId() == null ? DetailsMode.ADD : DetailsMode.EDIT;
        fromWalletList = Lists.newArrayList();
        toWalletList = Lists.newArrayList();
        categoryList = Lists.newArrayList();
    }

    private void setupAdapters() {
        toWallet.setAdapter(new WalletAdapter(getActivity(), toWalletList));
        fromWallet.setAdapter(new WalletAdapter(getActivity(), fromWalletList));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CASH_FLOW_DETAILS_STATE, cashFlowDetailsState);
        outState.putBoolean(IS_SELECTED, isSelected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillViewsWithData();
        setupListenersIfSelected();
        validateIfAmountContainsDigit();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        clearListeners();
        super.onPause();
    }

    private void setupListenersIfSelected() {
        if (isSelected) {
            setFragmentAsActive();
        }
    }

    private void fillViewsWithData() {
        fillWalletLists();
        fillCategoryList();
        fillViewsFromState();
        setDatePickerButtonTextFromState();
        setTimePickerButtonTextFromState();
    }

    abstract void fillWalletLists();

    abstract void fillCategoryList();

    protected void fillViewsFromState() {
        amount.setText(cashFlowDetailsState.getAmount());
        comment.setText(cashFlowDetailsState.getComment());
        category.setText(getCategoryText(getCategoryFromState()));
        fromWallet.setSelection(fromWalletList.indexOf(getFromWalletFromState()));
        ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
        toWallet.setSelection(toWalletList.indexOf(getToWalletFromState()));
        ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
    }

    private String getCategoryText(Category category) {
        if (category == null) {
            return "";
        } else if (category.getParent() == null) {
            return category.getName();
        }
        return category.getName() + " (" + category.getParent().getName() + ")";
    }

    abstract Category getCategoryFromState();

    abstract Wallet getFromWalletFromState();

    abstract Wallet getToWalletFromState();

    private void setDatePickerButtonTextFromState() {
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
    }

    private void setTimePickerButtonTextFromState() {
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
    }

    @OnItemSelected(R.id.fromWallet)
    abstract void onFromWalletItemSelected(int position);

    @OnItemSelected(R.id.toWallet)
    abstract void onToWalletItemSelected(int position);

    @OnClick(R.id.category)
    void onCategoryClick() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_list, null);
        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.list);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cashflowCategoryChooseAlertTitle))
                .setView(expandableListView)
                .create();

        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList, new OnContentClickListener<Category>() {
            @Override
            public void onContentClick(Category content) {
                storeSelectedCategoryInState(localCategoryService.findById(content.getId()));
                alertDialog.dismiss();
            }
        }));

        alertDialog.show();
    }

    abstract void storeSelectedCategoryInState(Category category);

    @OnClick(R.id.removeCategory)
    abstract void onRemoveCategoryClick();

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
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity())
        ).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            actionSave();
            return true;
        }
        return false;
    }

    public void actionSave() {
        if (preValidations() && handleActionSave()) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean preValidations() {
        return validateAmount();
    }

    private boolean validateAmount() {
        return validateIfAmountIsNullOrEmpty() && validateIfAmountContainsDigit();
    }

    private boolean validateIfAmountIsNullOrEmpty() {
        if (Strings.isNullOrEmpty(amount.getText().toString())) {
            amount.setError("Amount can't be empty.");
            return false;
        }
        return true;
    }

    private boolean validateIfAmountContainsDigit() {
        if (!isContainValidateDigit(amount.getText().toString())) {
            amount.setError("Write amount in this pattern: (+/-)149.1234");
            return false;
        }
        return true;
    }

    private boolean isContainValidateDigit(String s) {
        return s.matches(digitRegex);
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
        localCashFlowService.insert(getCashFlowFromState());
        return true;
    }

    private boolean tryUpdate() {
        localCashFlowService.update(getCashFlowFromState());
        return true;
    }

    private CashFlow getCashFlowFromState() {
        CashFlow cashFlow = new CashFlow();

        cashFlow.setId(cashFlowDetailsState.getId());
        cashFlow.setAmount(Double.parseDouble(cashFlowDetailsState.getAmount()));
        cashFlow.setDateTime(new Date(cashFlowDetailsState.getDate()));
        cashFlow.setComment(cashFlowDetailsState.getComment());
        cashFlow.setFromWallet(getFromWalletFromState());
        cashFlow.setToWallet(getToWalletFromState());
        cashFlow.setCategory(getCategoryFromState());

        return cashFlow;
    }

    public void setFragmentAsActive() {
        isSelected = true;
        setupListeners();
        comment.clearFocus();
        amount.clearFocus();
    }

    public void setFragmentAsInactive() {
        clearListeners();
        isSelected = false;

    }

    private void setupListeners() {
        commentTextWatcher = new CommentTextWatcher();
        comment.addTextChangedListener(commentTextWatcher);

        amountTextWatcher = new AmountTextWatcher();
        amount.addTextChangedListener(amountTextWatcher);
    }

    private void clearListeners() {
        amount.removeTextChangedListener(amountTextWatcher);
        amountTextWatcher = null;

        comment.removeTextChangedListener(commentTextWatcher);
        commentTextWatcher = null;
    }

    class CommentTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals(cashFlowDetailsState.getComment())) {
                cashFlowDetailsState.setComment(s.toString());
                notifyCashFlowStateChanged();
            }
        }
    }

    class AmountTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals(cashFlowDetailsState.getAmount())) {
                validateAmount();
                cashFlowDetailsState.setAmount(s.toString());
                notifyCashFlowStateChanged();
            }
        }
    }

    private void notifyCashFlowStateChanged() {
        bus.post(new CashFlowDetailsStatusChangedEvent(getClass()));
    }

    class WalletAdapter extends BaseAdapter {
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
                textView.setTextSize(context.getResources().getDimension(R.dimen.smallFontSize));
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
