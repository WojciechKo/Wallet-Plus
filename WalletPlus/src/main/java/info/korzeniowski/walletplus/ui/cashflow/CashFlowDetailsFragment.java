package info.korzeniowski.walletplus.ui.cashflow;

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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
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


public class CashFlowDetailsFragment extends Fragment {

    static final public String CASH_FLOW_ID = "CASH_FLOW_ID";

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

    @InjectView(R.id.recordType)
    Switch recordType;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @Inject @Named("local")
    WalletService localWalletService;

    @Inject @Named("local")
    CategoryService localCategoryService;

    @Inject @Named("amount")
    NumberFormat amountFormat;

    private DetailsType type;
    private CashFlow.Builder cashFlowBuilder;

    private Calendar calendar;
    private Category previousCategory;

    private List<Wallet> fromWalletList;
    private List<Wallet> toWalletList;
    private List<Category> categoryList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
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

    @OnClick(R.id.recordType)
    public void onClickRecordType() {
        recordTypeCheckedChanged();
    }

    @OnClick(R.id.datePicker)
    public void onClickDatePicker() {
        datePickerClicked();
    }

    @OnClick(R.id.category)
    public void onClickCategory() {
        categoryClicked();
    }

    @OnClick(R.id.timePicker)
    public void onClickTimePicker() {
        timePickerClicked();
    }

    @OnClick(R.id.removeCategory)
    public void onClickRemoveCategory() {
        cashFlowBuilder.setCategory(localCashFlowService.getOtherCategory());
        category.setText(getCategoryText(cashFlowBuilder.getCategory()));
    }

    void setupViews() {
        initFields();
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    private void initFields() {
        Category other = localCashFlowService.getOtherCategory();
        Long cashFlowId = getArguments().getLong(CASH_FLOW_ID);
        type = cashFlowId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        calendar = Calendar.getInstance();
        cashFlowBuilder = new CashFlow.Builder(localCashFlowService.findById(cashFlowId));
        if (type.equals(DetailsType.EDIT)) {
            calendar.setTime(cashFlowBuilder.getDateTime());
        } else if (type.equals(DetailsType.ADD)) {
            cashFlowBuilder.setCategory(other);
        }
        previousCategory = other;
        fromWalletList = Lists.newArrayList();
        toWalletList = Lists.newArrayList();
        categoryList = Lists.newArrayList();
    }

    private void setupAdapters() {
        toWallet.setAdapter(new WalletAdapter(getActivity(), toWalletList));
        fromWallet.setAdapter(new WalletAdapter(getActivity(), fromWalletList));
    }

    private void setupListeners() {
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isDecimal(s)) {
                    int numberOfDigitsToDelete = getNumberOfDigitsToDelete(s);
                    s.delete(s.length() - numberOfDigitsToDelete, s.length());
                }
            }

            private int getNumberOfDigitsToDelete(Editable s) {
                int allowedNumberOfDigitsAfterComa = 2;
                int indexOfComa = s.toString().indexOf('.');
                if (indexOfComa < s.length() - 1 - allowedNumberOfDigitsAfterComa) {
                    return s.length() - indexOfComa - 1 - allowedNumberOfDigitsAfterComa;
                }
                return 0;
            }

            private boolean isDecimal(Editable s) {
                return s.toString().contains(".");
            }
        });
    }

    private void fillViewsWithData() {
        resetDatePicker();
        resetTimePicker();
        if (cashFlowBuilder.getAmount() != null) {
            amount.setText(amountFormat.format(cashFlowBuilder.getAmount()));
        }
        comment.setText(cashFlowBuilder.getComment());
        recordType.setChecked(cashFlowBuilder.build().isExpanse());
        refillLists();
        fromWallet.setSelection(fromWalletList.indexOf(cashFlowBuilder.getFromWallet()));
        toWallet.setSelection(toWalletList.indexOf(cashFlowBuilder.getToWallet()));
        notifyWalletAdapters();
        if (cashFlowBuilder.getCategory() != null) {
            category.setText(getCategoryText(cashFlowBuilder.getCategory()));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == info.korzeniowski.walletplus.R.id.menu_save) {
            actionSave();
            return true;
        }
        return false;
    }

    void recordTypeCheckedChanged() {
        handleChangeCategory();
        notifyWalletAdapters();
    }

    private void handleChangeCategory() {
        swapCategoryWithPrevious();
        category.setText(getCategoryText(cashFlowBuilder.getCategory()));

        int selectedFromWalletPosition = fromWallet.getSelectedItemPosition();
        int selectedToWalletPosition = toWallet.getSelectedItemPosition();

        refillLists();

        toWallet.setSelection(selectedFromWalletPosition);
        fromWallet.setSelection(selectedToWalletPosition);
    }

    private void swapCategoryWithPrevious() {
        Category temp = previousCategory;
        previousCategory = cashFlowBuilder.getCategory();
        cashFlowBuilder.setCategory(temp);
    }

    private String getCategoryText(Category category) {
        if (category.getParent() == null) {
            return category.getName();
        }
        return category.getName() + " (" + category.getParent().getName() + ")";
    }

    private void refillLists() {
        fromWalletList.clear();
        toWalletList.clear();
        categoryList.clear();
        fillWalletLists();
        fillCategoryList();
    }

    private void fillWalletLists() {
        if (isExpanseType()) {
            fromWalletList.addAll(localWalletService.getMyWallets());
            toWalletList.addAll(localWalletService.getContractors());
        } else {
            fromWalletList.addAll(localWalletService.getContractors());
            toWalletList.addAll(localWalletService.getMyWallets());
        }
    }

    private void fillCategoryList() {
        if (isExpanseType()) {
            categoryList.addAll(localCategoryService.getMainExpenseTypeCategories());
        } else {
            categoryList.addAll(localCategoryService.getMainIncomeTypeCategories());
        }
    }

    public boolean isExpanseType() {
        return recordType.isChecked();
    }

    private void notifyWalletAdapters() {
        ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
        ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
    }

    void categoryClicked() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_list, null);
        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.superList);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cashflowCategoryChooseAlertTitle))
                .setView(expandableListView)
                .create();

        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList, new OnContentClickListener() {
            @Override
            public void onContentClick(Long id) {
                cashFlowBuilder.setCategory(localCategoryService.findById(id));
                category.setText(getCategoryText(cashFlowBuilder.getCategory()));
                alertDialog.dismiss();
            }
        }));

        alertDialog.show();
    }

    private void resetDatePicker() {
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(calendar.getTime()));
    }

    private void resetTimePicker() {
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(calendar.getTime()));
    }

    void datePickerClicked() {
        new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        resetDatePicker();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    void timePickerClicked() {
        new TimePickerDialog(
                getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        resetTimePicker();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity())
        ).show();
    }

    void actionSave() {
        if (preValidations()) {
            getDataFromViews();
            boolean success = false;
            if (DetailsType.ADD.equals(type)) {
                success = tryInsert();
            } else if (DetailsType.EDIT.equals(type)) {
                success = tryUpdate();
            }
            if (success) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    public void getDataFromViews() {
        cashFlowBuilder.setAmount(Float.parseFloat(amount.getText().toString()));
        cashFlowBuilder.setDateTime(calendar.getTime());
        cashFlowBuilder.setFromWallet((Wallet) fromWallet.getSelectedItem());
        cashFlowBuilder.setToWallet((Wallet) toWallet.getSelectedItem());
        cashFlowBuilder.setComment(comment.getText().toString());
    }

    private boolean tryInsert() {
        localCashFlowService.insert(cashFlowBuilder.build());
        return true;
    }

    private boolean tryUpdate() {
        localCashFlowService.update(cashFlowBuilder.build());
        return true;
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

    private enum DetailsType {ADD, EDIT}

    private class WalletAdapter extends BaseAdapter {
        List<Wallet> wallets;
        Context context;

        private WalletAdapter(Context context, List<Wallet> list) {
            super();
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
