package info.korzeniowski.walletplus.ui.cashflow.details;

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
import android.widget.Toast;

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
import info.korzeniowski.walletplus.ui.category.list.CategoryExpandableListAdapter;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

public class CashFlowDetailsFragment extends Fragment {
    public static final String TAG = "CashFlowDetailsFragment";

    public static final String CASH_FLOW_ID = "CASH_FLOW_ID";

    public static final String CASH_FLOW_DETAILS_STATE = "cashFlowDetailsState";

    public static final String digitRegex = "^(\\+|\\-)?(([0-9]+(\\.[0-9]{0,4})?)|(\\.[0-9]{0,4}))$";

    @InjectView(R.id.fromWallet)
    Spinner fromWallet;

    @InjectView(R.id.toWallet)
    Spinner toWallet;

    @InjectView(R.id.typeToggle)
    FButton typeToggle;

    @InjectView(R.id.transferToggle)
    FButton transferToggle;

    @InjectView(R.id.amount)
    EditText amount;

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

    private DetailsMode detailsMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        cashFlowDetailsState = initOrRestoreState(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private CashFlowDetailsParcelableState initOrRestoreState(Bundle savedInstanceState) {
        CashFlowDetailsParcelableState restoredState = savedInstanceState != null
                ? (CashFlowDetailsParcelableState) savedInstanceState.getParcelable(CASH_FLOW_DETAILS_STATE)
                : null;

        return restoredState != null
                ? restoredState
                : initState();
    }

    private CashFlowDetailsParcelableState initState() {
        Long cashFlowId = getArguments() != null
                ? getArguments().getLong(CASH_FLOW_ID)
                : 0;

        return cashFlowId != 0
                ? getStateFromCashFlow(localCashFlowService.findById(cashFlowId))
                : new CashFlowDetailsParcelableState();
    }

    private CashFlowDetailsParcelableState getStateFromCashFlow(CashFlow cashFlow) {
        return cashFlow != null
                ? new CashFlowDetailsParcelableState(cashFlow)
                : new CashFlowDetailsParcelableState();
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
        fillViewsWithDataFromState();
    }

    protected void initFields() {
        detailsMode = cashFlowDetailsState.getId() == null ? DetailsMode.ADD : DetailsMode.EDIT;
        categoryList = localCategoryService.getMainCategories();
        myWallets = localWalletService.getMyWallets();
        otherWallets = localWalletService.getContractors();
    }

    private void fillViewsWithDataFromState() {
        setToggles();
        fillWallets();
        amount.setText(cashFlowDetailsState.getAmount());
        comment.setText(cashFlowDetailsState.getComment());
        setCategory();
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
    }

    private void setToggles() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            setTogglesOnIncome();
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            setTogglesOnExpanse();
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            setTogglesOnTransfer();
        }
    }

    private void fillWallets() {
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

    private void setCategory() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            category.setVisibility(View.GONE);
            categoryLabel.setVisibility(View.GONE);
            removeCategory.setVisibility(View.GONE);
        } else {
            category.setVisibility(View.VISIBLE);
            categoryLabel.setVisibility(View.VISIBLE);
            removeCategory.setVisibility(View.VISIBLE);
            category.setText(getCategoryText(getCategoryFromState()));
        }
    }

    private void setTogglesOnIncome() {
        typeToggle.setText(getString(R.string.income));
        typeToggle.setTextColor(getResources().getColor(R.color.white));
        typeToggle.setButtonColor(getResources().getColor(R.color.green));

        transferToggle.setTextColor(getResources().getColor(R.color.black));
        transferToggle.setButtonColor(getResources().getColor(R.color.whiteE5));
    }

    private void setTogglesOnExpanse() {
        typeToggle.setText(getString(R.string.expanse));
        typeToggle.setTextColor(getResources().getColor(R.color.white));
        typeToggle.setButtonColor(getResources().getColor(R.color.red));

        transferToggle.setTextColor(getResources().getColor(R.color.black));
        transferToggle.setButtonColor(getResources().getColor(R.color.whiteE5));
    }

    private void setTogglesOnTransfer() {
        if (cashFlowDetailsState.getPreviousType() == CashFlow.Type.INCOME) {
            typeToggle.setText(getString(R.string.income));
        } else if (cashFlowDetailsState.getPreviousType() == CashFlow.Type.EXPANSE) {
            typeToggle.setText(getString(R.string.expanse));
        }
        typeToggle.setTextColor(getResources().getColor(R.color.black));
        typeToggle.setButtonColor(getResources().getColor(R.color.whiteE5));

        transferToggle.setTextColor(getResources().getColor(R.color.white));
        transferToggle.setButtonColor(getResources().getColor(R.color.blue));
    }

    private void setupFromWallet(List<Wallet> wallets) {
        fromWallet.setAdapter(new WalletAdapter(getActivity(), wallets));
        fromWallet.setSelection(wallets.indexOf(getFromWalletFromState()));
        ((WalletAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
    }

    private void setupToWallet(List<Wallet> wallets) {
        toWallet.setAdapter(new WalletAdapter(getActivity(), wallets));
        toWallet.setSelection(wallets.indexOf(getToWalletFromState()));
        ((WalletAdapter) toWallet.getAdapter()).notifyDataSetChanged();
    }

    private String getCategoryText(Category category) {
        if (category == null) {
            return getString(R.string.categoryNoCategoryName);
        } else if (category.getParent() == null) {
            return category.getName();
        }
        return category.getName() + " (" + category.getParent().getName() + ")";
    }

    private Category getCategoryFromState() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            return cashFlowDetailsState.getIncomeCategory();
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            return cashFlowDetailsState.getExpanseCategory();
        }
        return null;
    }

    private Wallet getFromWalletFromState() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            return cashFlowDetailsState.getIncomeFromWallet();
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE ||
                cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            return cashFlowDetailsState.getExpanseFromWallet();
        }
        return null;
    }

    private Wallet getToWalletFromState() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME ||
                cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            return cashFlowDetailsState.getIncomeToWallet();
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            return cashFlowDetailsState.getExpanseToWallet();
        }
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CASH_FLOW_DETAILS_STATE, cashFlowDetailsState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            actionSave();
            return true;
        }
        return false;
    }

    /**
     * ***********
     * LISTENERS *
     * ***********
     */

    @OnClick(R.id.typeToggle)
    void typeToggleClicked() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            cashFlowDetailsState.setType(CashFlow.Type.EXPANSE);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            cashFlowDetailsState.setType(CashFlow.Type.INCOME);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.setType(cashFlowDetailsState.getPreviousType());
        }
        fillViewsWithDataFromState();
    }

    @OnClick(R.id.transferToggle)
    void transferToggleClicked() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.setType(cashFlowDetailsState.getPreviousType());
        } else {
            cashFlowDetailsState.setType(CashFlow.Type.TRANSFER);
        }
        fillViewsWithDataFromState();
    }

    @OnItemSelected(R.id.fromWallet)
    void onFromWalletItemSelected(int position) {
        Wallet selected = (Wallet) fromWallet.getItemAtPosition(position);
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            cashFlowDetailsState.setIncomeFromWallet(selected);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE ||
                cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.setExpanseFromWallet(selected);
        }
    }

    @OnItemSelected(R.id.toWallet)
    void onToWalletItemSelected(int position) {
        Wallet selected = (Wallet) toWallet.getItemAtPosition(position);
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME ||
                cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            cashFlowDetailsState.setIncomeToWallet(selected);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            cashFlowDetailsState.setExpanseToWallet(selected);
        }
    }

    @OnClick(R.id.category)
    void onCategoryClick() {
        //TODO: Should null be fixed?
        ExpandableListView expandableListView = (ExpandableListView) LayoutInflater.from(getActivity()).inflate(R.layout.category_list, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cashflowCategoryChooseAlertTitle))
                .setView(expandableListView)
                .create();

        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList, new OnContentClickListener<Category>() {
            @Override
            public void onContentClick(Category content) {
                setSelectedCategoryInState(localCategoryService.findById(content.getId()));
                alertDialog.dismiss();
            }
        }));

        alertDialog.show();
    }

    void setSelectedCategoryInState(Category category) {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            cashFlowDetailsState.setIncomeCategory(category);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            cashFlowDetailsState.setExpanseCategory(category);
        }
    }

    @OnClick(R.id.removeCategory)
    void onRemoveCategoryClick() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            cashFlowDetailsState.setIncomeCategory(null);
        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            cashFlowDetailsState.setExpanseCategory(null);
        }
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

    @OnTextChanged(value = R.id.amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAmountTextChanged(Editable s) {
        if (!s.toString().equals(cashFlowDetailsState.getAmount())) {
            validateAmount();
            cashFlowDetailsState.setAmount(s.toString());
        }
    }

    private boolean validateAmount() {
        return validateIfAmountIsNullOrEmpty() && validateIfAmountContainsDigit();
    }

    private boolean validateIfAmountIsNullOrEmpty() {
        if (Strings.isNullOrEmpty(cashFlowDetailsState.getAmount())) {
            amount.setError("Amount can't be empty.");
            return false;
        }
        return true;
    }

    private boolean validateIfAmountContainsDigit() {
        if (!isContainValidateDigit(cashFlowDetailsState.getAmount())) {
            amount.setError("Write amount in this pattern: (+/-)149.1234");
            return false;
        }
        return true;
    }

    private boolean isContainValidateDigit(String s) {
        return s.matches(digitRegex);
    }

    @OnTextChanged(value = R.id.comment, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onCommentTextChanged(Editable s) {
        if (!s.toString().equals(cashFlowDetailsState.getComment())) {
            cashFlowDetailsState.setComment(s.toString());
        }
    }

    public void actionSave() {
        if (preValidations() && handleActionSave()) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean preValidations() {
        boolean result = validateAmount();
        if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            result &= validateIfFromWalletIsDifferentThanToWallet();
        }
        return result;
    }

    private boolean validateIfFromWalletIsDifferentThanToWallet() {
        if (getFromWalletFromState() == getToWalletFromState()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.cashFlowFromAndToWalletsNeedToBeDifferent), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean handleActionSave() {

        if (detailsMode == DetailsMode.ADD) {
            return tryInsert();
        } else if (detailsMode == DetailsMode.EDIT) {
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

    private enum DetailsMode {ADD, EDIT}

    class WalletAdapter extends BaseAdapter {
        List<Wallet> wallets;

        WeakReference<Context> context;

        private WalletAdapter(Context context, List<Wallet> list) {
            this.context = new WeakReference<Context>(context);
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
