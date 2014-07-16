package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.CashFlowDataManager;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.drawermenu.category.CategoryExpandableListAdapter;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

@EFragment(R.layout.cashflow_details_fragment)
@OptionsMenu(R.menu.action_save)
public class DetailsOfRegularCashFlowFragment extends Fragment {

    static final public String CASH_FLOW_ID = "CASH_FLOW_ID";

    @ViewById
    Spinner fromWallet;

    @ViewById
    EditText amount;

    @ViewById
    Switch recordType;

    @ViewById
    Button category;

    @ViewById
    Spinner toWallet;

    @ViewById
    Button datePicker;

    @ViewById
    Button timePicker;

    @Inject @Named("local")
    CashFlowDataManager localCashFlowDataManager;

    @Inject @Named("local")
    WalletDataManager localWalletDataManager;

    @Inject @Named("local")
    CategoryDataManager localCategoryDataManager;

    private CashFlow cashFlow;
    private Long cashFlowId;
    private DetailsType type;
    private Calendar calendar;
    private Category selectedCategory;
    private Category previousCategory;

    private List<Wallet> fromWalletList = Lists.newArrayList();
    private List<Wallet> toWalletList = Lists.newArrayList();
    private List<Category> categoryList = Lists.newArrayList();

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cashFlowId = getArguments().getLong(CASH_FLOW_ID);
        type = cashFlowId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        if (type.equals(DetailsType.ADD)) {
            cashFlow = new CashFlow();
        } else if (type.equals(DetailsType.EDIT)) {
            cashFlow = localCashFlowDataManager.findById(cashFlowId);
        }
        calendar = Calendar.getInstance();
        return null;
    }

    @AfterViews
    void setupViews() {
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    @CheckedChange
    void recordTypeCheckedChanged() {
        refreshDataInLists();
        Category temp = previousCategory;
        previousCategory = selectedCategory;
        selectedCategory = temp;
        if (selectedCategory == null) {
            category.setText(R.string.cashflowCategoryHint);
        } else {
            category.setText(selectedCategory.getName());
        }
    }

    private void setupAdapters() {
        toWallet.setAdapter(new WalletAdapter(getActivity(), toWalletList));
        fromWallet.setAdapter(new WalletAdapter(getActivity(), fromWalletList));
        refreshDataInLists();
    }

    private void refreshDataInLists() {
        fromWalletList.clear();
        toWalletList.clear();
        categoryList.clear();
        if (isExpanseType()) {
            fromWalletList.addAll(localWalletDataManager.getMyWallets());
            toWalletList.addAll(localWalletDataManager.getContractors());
            categoryList.addAll(localCategoryDataManager.getMainExpenseTypeCategories());
        } else {
            fromWalletList.addAll(localWalletDataManager.getContractors());
            toWalletList.addAll(localWalletDataManager.getMyWallets());
            categoryList.addAll(localCategoryDataManager.getMainIncomeTypeCategories());
        }
        ((ArrayAdapter) fromWallet.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) toWallet.getAdapter()).notifyDataSetChanged();
    }

    public boolean isExpanseType() {
        //TODO: do it better
        return recordType.isChecked();
    }

    private void setupListeners() {
    }

    @Click
    void categoryClicked() {
        ExpandableListView expandableListView = new ExpandableListView(getActivity());
        expandableListView.setAdapter(new CategoryExpandableListAdapter(getActivity(), categoryList));

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Choose category:")
                .setView(expandableListView)
                .create();

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = ((ExpandableListView) parent).getExpandableListPosition(position);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

                ExpandableListAdapter expandableListAdapter = ((ExpandableListView) parent).getExpandableListAdapter();
                if (childPosition == -1)
                    selectedCategory = (Category) expandableListAdapter.getGroup(groupPosition);
                else
                    selectedCategory = (Category) expandableListAdapter.getChild(groupPosition, childPosition);

                category.setText(selectedCategory.getName());
                alertDialog.dismiss();
                return true;
            }
        });

        alertDialog.show();
    }

    private void fillViewsWithData() {
        refreshDatePicker();
        refreshTimePicker();
        if (cashFlow.getAmount() != null) {
            amount.setText(new DecimalFormat(getActivity().getString(R.string.amountFormat)).format(cashFlow.getAmount()));
        }
        fromWallet.setSelection(fromWalletList.indexOf(cashFlow.getFromWallet()));
        toWallet.setSelection(toWalletList.indexOf(cashFlow.getToWallet()));
        if (cashFlow.getCategory() != null) {
            category.setText(cashFlow.getCategory().getName());
        }
    }

    private void refreshDatePicker() {
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(calendar.getTime()));
    }

    private void refreshTimePicker() {
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(calendar.getTime()));
    }

    public CashFlow getDataFromViews() {
        cashFlow.setAmount(Float.parseFloat(amount.getText().toString()));
        cashFlow.setId(cashFlowId);
        cashFlow.setDateTime(calendar.getTime());
        cashFlow.setCategory(selectedCategory);
        cashFlow.setFromWallet((Wallet) fromWallet.getSelectedItem());
        cashFlow.setToWallet((Wallet) toWallet.getSelectedItem());
        return cashFlow;
    }

    @Click
    void datePickerClicked() {
        new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        refreshDatePicker();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @Click
    void timePickerClicked() {
        new TimePickerDialog(
                getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        refreshTimePicker();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity())
        ).show();
    }

    @OptionsItem(R.id.menu_save)
    void actionSave() {
        cashFlow = getDataFromViews();
        if (DetailsType.ADD.equals(type)) {
            localCashFlowDataManager.insert(cashFlow);
        } else if (DetailsType.EDIT.equals(type)) {
            localCashFlowDataManager.update(cashFlow);
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private enum DetailsType {ADD, EDIT}

    private class WalletAdapter extends ArrayAdapter<Wallet> {

        private WalletAdapter(Context context, List<Wallet> list) {
            super(context, android.R.layout.simple_list_item_1, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position).getName());
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

}
