package info.korzeniowski.walletplus.ui.cashflow.details;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

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

public class CashFlowDetailsFragment extends Fragment {
    public static final String TAG = "CashFlowDetailsFragment";
    private static final String ARGUMENT_CASH_FLOW_ID = "CASH_FLOW_ID";
    private static final String CASH_FLOW_DETAILS_STATE = "cashFlowDetailsState";

    @InjectView(R.id.wallet)
    Spinner wallet;

    @InjectView(R.id.typeToggle)
    FButton typeToggle;

    @InjectView(R.id.amountLabel)
    TextView amountLabel;

    @InjectView(R.id.amount)
    EditText amount;

    @InjectView(R.id.category)
    MultiAutoCompleteTextView category;

    @InjectView(R.id.categoryLabel)
    TextView categoryLabel;

    @InjectView(R.id.datePicker)
    Button datePicker;

    @InjectView(R.id.timePicker)
    Button timePicker;

    @InjectView(R.id.extraLabel)
    TextView extraLabel;

    @InjectView(R.id.isCompleted)
    CheckedTextView isCompleted;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private List<Wallet> wallets;
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

        categoryList = localCategoryService.getAll();
        wallets = localWalletService.getMyWallets();
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
        View view = inflater.inflate(R.layout.fragment_cash_flow_details, container, false);
        ButterKnife.inject(this, view);

        setupTypeDependentViews();
        if (detailsAction == DetailsAction.EDIT) {
            amount.setText(Strings.nullToEmpty(cashFlowDetailsState.getAmount()));
            category.setText(cashFlowDetailsState.getCategories());
            resetTagSpans(category.getEditableText());
        }
        wallet.setAdapter(new WalletAdapter(getActivity(), wallets));
        wallet.setSelection(wallets.indexOf(cashFlowDetailsState.getWallet()));

        isCompleted.setChecked(cashFlowDetailsState.isCompleted());
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(new Date(cashFlowDetailsState.getDate())));

        List<String> categoryNameList = Lists.transform(categoryList, new Function<Category, String>() {
            @Override
            public String apply(Category input) {
                return input.getName();
            }
        });
        category.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, categoryNameList));
        category.setTokenizer(new SpaceTokenizer());

        getActivity().findViewById(R.id.focusable).requestFocus();
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
        if (CashFlow.Type.INCOME.equals(cashFlowDetailsState.getType())) {
            cashFlowDetailsState.setType(CashFlow.Type.EXPANSE);
        } else if (CashFlow.Type.EXPANSE.equals(cashFlowDetailsState.getType())) {
            cashFlowDetailsState.setType(CashFlow.Type.INCOME);
        }
        setupTypeDependentViews();
    }

    @OnItemSelected(R.id.wallet)
    void onWalletItemSelected(int position) {
        Wallet selected = (Wallet) wallet.getItemAtPosition(position);
        cashFlowDetailsState.setWallet(selected);
    }

    @OnTextChanged(value = R.id.category, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAfterCategoryChanged(Editable s) {
        if (!cashFlowDetailsState.getCategories().equals(category.getText().toString())) {
            resetTagSpans(s);
            cashFlowDetailsState.setCategories(s.toString());
        }
    }

    private void resetTagSpans(Editable s) {
        ImageSpan[] spans = s.getSpans(0, s.length(), ImageSpan.class);
        for (int i = 0 ; i < spans.length ; i++) {
            s.removeSpan(spans[i]);
        }

        int start = 0;
        for (int i = 0 ; i < s.length() ; i++) {
            if (s.charAt(i) == ' ') {
                if (i - 1 >= start) {
                    ImageSpan imageSpan = new ImageSpan(convertViewToDrawable(createTagTextView(s.subSequence(start, i).toString())));
                    s.setSpan(imageSpan, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                start = i + 1;
            }
        }
    }

    private TextView createTagTextView(final String categoryName) {
        //creating textview dynamically
        final TextView tv = new TextView(getActivity());
        tv.setText(categoryName);
        tv.setTextSize(getResources().getDimension(R.dimen.mediumFontSize));
        Drawable drawable = getResources().getDrawable(R.drawable.oval);
        drawable.setColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC);
        tv.setBackground(drawable);
        tv.setTextColor(Color.WHITE);
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0);
        tv.setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.tagCrossPadding));
        return tv;
    }

    private static BitmapDrawable convertViewToDrawable(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();

        BitmapDrawable bitmapDrawable = new BitmapDrawable(viewBmp);
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());

        return bitmapDrawable;
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

    @OnClick(R.id.isCompleted)
    public void isCompletedToggle() {
        isCompleted.toggle();
        cashFlowDetailsState.setCompleted(isCompleted.isChecked());
    }

    private void setupTypeDependentViews() {
        setupToggles();
    }

    private void setupToggles() {
        if (cashFlowDetailsState.getType() == CashFlow.Type.INCOME) {
            typeToggle.setText(getString(R.string.income));
            typeToggle.setTextColor(getResources().getColor(R.color.white));
            typeToggle.setButtonColor(getResources().getColor(R.color.green));

        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPANSE) {
            typeToggle.setText(getString(R.string.expanse));
            typeToggle.setTextColor(getResources().getColor(R.color.white));
            typeToggle.setButtonColor(getResources().getColor(R.color.red));

        } else if (cashFlowDetailsState.getType() == CashFlow.Type.TRANSFER) {
            typeToggle.setText(getPreviousTypeName());
            typeToggle.setTextColor(getResources().getColor(R.color.black));
            typeToggle.setButtonColor(getResources().getColor(R.color.whiteE5));

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            onSaveOptionSelected();
            return true;
        }
        return false;
    }

    private void onSaveOptionSelected() {
        boolean isValid = true;

        if (Strings.isNullOrEmpty(cashFlowDetailsState.getAmount())) {
            amount.setError("Amount can't be empty.");
            isValid = false;
        } else if (!cashFlowDetailsState.isAmountValid()) {
            amount.setError("Write amount in this pattern: [+|-] 149.1234");
            isValid = false;
        }

        if (isValid) {
            if (DetailsAction.ADD.equals(detailsAction)) {
                localCashFlowService.insert(cashFlowDetailsState.buildCashFlow());
            } else if (DetailsAction.EDIT.equals(detailsAction)) {
                localCashFlowService.update(cashFlowDetailsState.buildCashFlow());
            }
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

    private enum DetailsAction {ADD, EDIT}

    public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {

        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + " ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }
    }


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
