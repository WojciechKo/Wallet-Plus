package info.korzeniowski.walletplus.ui.cashflow.details.tab;

import android.app.Activity;
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
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsEvent;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsParcelableState;
import info.korzeniowski.walletplus.ui.cashflow.details.OnCashFlowDetailsChangedListener;
import info.korzeniowski.walletplus.ui.category.list.CategoryExpandableListAdapter;
import info.korzeniowski.walletplus.widget.OnContentClickListener;


public abstract class CashFlowBaseDetailsFragment extends Fragment implements OnCashFlowDetailsChangedListener {
    public static final String CASH_FLOW_DETAILS_STATE = "cashFlowDetailsState";
    private TextWatcher textWatcher;
    private OnCashFlowDetailsChangedListener listener;

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
    @Named("amount")
    NumberFormat amountFormat;

    @Inject
    Bus bus;

    Object busEventHandler;

    private DetailsMode detailsMode;
    protected List<Wallet> fromWalletList;
    protected List<Wallet> toWalletList;
    protected List<Category> categoryList;
    protected CashFlowDetailsParcelableState cashFlowDetailsState;

    /////////////////////////////////////////
    // change handlers
//    @Subscribe
//    public void onAmountChanged(CashFlowDetailsEvent.AmountChanged event) {
//        String amountString = getAmountStringFromState();
//        if (!amount.getText().toString().equals(amountString)) {
//            amount.setText(amountString);
//        }
//    }


    @Override
    public void onAttach(Activity activity) {
        ((MainActivity) activity).addOnCashFlowDetailsChangedListener(this);
        super.onAttach(activity);
    }


    private String getAmountStringFromState() {
        Float amount = cashFlowDetailsState.getAmount();
        if (amount == null) {
            return "";
        }
        return amountFormat.format(amount);
    }

    public void onCommentChanged() {
        String commentString = cashFlowDetailsState.getComment();
        if (!comment.getText().toString().equals(commentString)) {
            comment.setText(commentString);
        }
    }

//
//    @Subscribe
//    public void onCommentChanged(CashFlowDetailsEvent.CommentChanged event) {
//        String commentString = cashFlowDetailsState.getComment();
//        if (!comment.getText().toString().equals(commentString)) {
//            comment.setText(commentString);
//        }
//    }
//
//    @Subscribe
//    public void onCategoryChanged(CashFlowDetailsEvent.CategoryChanged event) {
//        category.setText(getCategoryText(getCategoryFromState()));
//    }
//
//    @Subscribe
//    public void onFromWalletChanged(CashFlowDetailsEvent.FromWalletChanged event) {
//        if (fromWallet != null) {
//            fromWallet.setSelection(fromWalletList.indexOf(getFromWalletFromState()));
//            ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
//        }
//    }

    abstract Wallet getFromWalletFromState();
//
//    @Subscribe
//    public void onToWalletChanged(CashFlowDetailsEvent.ToWalletChanged event) {
//        if (toWallet != null) {
//            toWallet.setSelection(toWalletList.indexOf(getToWalletFromState()));
//            ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
//        }
//    }

    abstract Wallet getToWalletFromState();
//
//    @Subscribe
//    public void onDateChanged(CashFlowDetailsEvent.DateChanged event) {
//        setDatePickerButtonTextFromState();
//    }
//
//    @Subscribe
//    public void onTimeChanged(CashFlowDetailsEvent.TimeChanged event) {
//        setTimePickerButtonTextFromState();
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        cashFlowDetailsState = getArguments().getParcelable(CASH_FLOW_DETAILS_STATE);
        initState(cashFlowDetailsState);
        setHasOptionsMenu(true);
    }

    protected void initState(CashFlowDetailsParcelableState cashFlowDetailsState) {
        if (!cashFlowDetailsState.isInit() && cashFlowDetailsState.getId() == 0) {
            cashFlowDetailsState.setDate(Calendar.getInstance().getTimeInMillis());
            cashFlowDetailsState.setIncomeCategory(localCashFlowService.getOtherCategory());
            cashFlowDetailsState.setExpanseCategory(localCashFlowService.getOtherCategory());
            cashFlowDetailsState.setInit(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        busEventHandler = new CashFlowEventHandler();
        bus.register(busEventHandler);
        listener = (OnCashFlowDetailsChangedListener) getActivity();
//        ((MainActivity) getActivity()).addOnCashFlowDetailsChangedListener(this);
    }


    @Override
    public void onPause() {
        comment.removeTextChangedListener(textWatcher);
        textWatcher = null;
        bus.unregister(busEventHandler);
        busEventHandler = null;
        ((MainActivity) getActivity()).removeOnCashFlowDetailsChangedListeners(this);
        listener = null;
        super.onPause();
//        busEventHandler = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.cashflow_details_fragment, container, false);
        ButterKnife.inject(this, view);
        setupViews();

        textWatcher = new TextWatcher() {
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
                    if (listener != null) listener.onCommentChanged();
//                    bus.post(new CashFlowDetailsEvent.CommentChanged(cashFlowDetailsState.getComment()));
                }
            }
        };
        comment.addTextChangedListener(textWatcher);
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

    abstract void fillWalletLists();

    abstract void fillCategoryList();

    @OnItemSelected(R.id.fromWallet)
    abstract void onFromWalletItemSelected(int position);

    @OnItemSelected(R.id.toWallet)
    abstract void onToWalletItemSelected(int position);

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
        bus.post(new CashFlowDetailsEvent.AmountChanged());
//        onCashFlowDetailsChangedListener.onAmountChanged();
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

//    @OnTextChanged(value = R.id.comment, callback = OnTextChanged.Callback.TEXT_CHANGED)
//    void onCommentAfterTextChanged(CharSequence s) {
//        if (!s.toString().equals(cashFlowDetailsState.getComment())) {
//            cashFlowDetailsState.setComment(s.toString());
//            bus.post(new CashFlowDetailsEvent.CommentChanged(cashFlowDetailsState.getComment()));
//        }
////        onCashFlowDetailsChangedListener.onCommentChanged();
//    }

    @OnClick(R.id.category)
    public void onCategoryClick() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_list, null);
        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.list);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cashflowCategoryChooseAlertTitle))
                .setView(expandableListView)
                .create();

        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList, new OnContentClickListener() {
            @Override
            public void onContentClick(Long id) {
                storeSelectedCategoryInState(localCategoryService.findById(id));
                bus.post(new CashFlowDetailsEvent.CategoryChanged());
//                onCashFlowDetailsChangedListener.onCategoryChanged();
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
                        bus.post(new CashFlowDetailsEvent.DateChanged());
//                        onCashFlowDetailsChangedListener.onDateChanged();
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
                        bus.post(new CashFlowDetailsEvent.TimeChanged());
//                        onCashFlowDetailsChangedListener.onTimeChanged();
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
        if (item.getItemId() == R.id.menu_save) {
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
        localCashFlowService.insert(getCashFlowFromState());
        return true;
    }

    private boolean tryUpdate() {
        localCashFlowService.update(getCashFlowFromState());
        return true;
    }

    abstract CashFlow getCashFlowFromState();

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

    private class CashFlowEventHandler {

        @Subscribe
        public void onAmountChanged(CashFlowDetailsEvent.AmountChanged event) {
            String amountString = getAmountStringFromState();
            if (!amount.getText().toString().equals(amountString)) {
                amount.setText(amountString);
            }
        }
//
//        @Subscribe
//        public void onCommentChanged(CashFlowDetailsEvent.CommentChanged event) {
//            String commentString = cashFlowDetailsState.getComment();
//            if (!comment.getText().toString().equals(commentString)) {
//                comment.setText(commentString);
//            }
//        }

        @Subscribe
        public void onCategoryChanged(CashFlowDetailsEvent.CategoryChanged event) {
            category.setText(getCategoryText(getCategoryFromState()));
        }

        @Subscribe
        public void onFromWalletChanged(CashFlowDetailsEvent.FromWalletChanged event) {
            if (fromWallet != null) {
                fromWallet.setSelection(fromWalletList.indexOf(getFromWalletFromState()));
                ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
            }
        }

        @Subscribe
        public void onToWalletChanged(CashFlowDetailsEvent.ToWalletChanged event) {
            if (toWallet != null) {
                toWallet.setSelection(toWalletList.indexOf(getToWalletFromState()));
                ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
            }
        }

        @Subscribe
        public void onDateChanged(CashFlowDetailsEvent.DateChanged event) {
            setDatePickerButtonTextFromState();
        }

        @Subscribe
        public void onTimeChanged(CashFlowDetailsEvent.TimeChanged event) {
            setTimePickerButtonTextFromState();
        }
    }
}
