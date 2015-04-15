package pl.net.korzeniowski.walletplus.ui.cashflow.details;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import info.hoang8f.widget.FButton;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.WalletPlus;
import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.model.Wallet;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.service.TagService;
import pl.net.korzeniowski.walletplus.service.WalletService;
import pl.net.korzeniowski.walletplus.util.PrefUtils;

import static pl.net.korzeniowski.walletplus.util.KorzeniowskiUtils.Views.dipToPixels;

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

    @InjectView(R.id.tag)
    MultiAutoCompleteTextView tag;

    @InjectView(R.id.tagLabel)
    TextView tagLabel;

    @InjectView(R.id.datePicker)
    Button datePicker;

    @InjectView(R.id.timePicker)
    Button timePicker;

    @InjectView(R.id.extraLabel)
    TextView extraLabel;

    @InjectView(R.id.isCompleted)
    CheckedTextView isCompleted;

    @Inject
    CashFlowService cashFlowService;

    @Inject
    WalletService walletService;

    @Inject
    TagService tagService;

    @Inject
    PrefUtils prefUtils;

    private List<Wallet> wallets;
    private List<Tag> tags;

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

        ((WalletPlus) getActivity().getApplication()).component().inject(this);

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

        tags = tagService.getAll();
        wallets = walletService.getAll();
    }

    private CashFlowDetailsParcelableState initCashFlowDetailsState(Long cashFlowId) {
        if (detailsAction == DetailsAction.ADD) {
            return new CashFlowDetailsParcelableState();
        } else if (detailsAction == DetailsAction.EDIT) {
            return new CashFlowDetailsParcelableState(cashFlowService.findById(cashFlowId));
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
            tag.setText(cashFlowDetailsState.getTags());
            resetTagSpans(tag.getEditableText());
        }
        wallet.setAdapter(new WalletAdapter(getActivity(), wallets));
        wallet.setSelection(wallets.indexOf(cashFlowDetailsState.getWallet()));

        isCompleted.setChecked(cashFlowDetailsState.isCompleted());
        Date date = new Date(cashFlowDetailsState.getDate());
        datePicker.setText(DateFormat.getDateFormat(getActivity()).format(date));
        timePicker.setText(DateFormat.getTimeFormat(getActivity()).format(date));

        tag.setDropDownBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tag.setAdapter(getAdapter(tags));
        tag.setTokenizer(new SpaceTokenizer());
        tag.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder();
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (isAllowed(c)) {
                        builder.append(c);
                    }
                }
                return builder.toString();
            }

            private boolean isAllowed(char c) {
                return Character.isLetterOrDigit(c) || c == ' ' || c == '-';
            }
        }});

        getActivity().findViewById(R.id.focusable).requestFocus();
        return view;
    }

    private TagAdapter getAdapter(List<Tag> tags) {
        return new TagAdapter(tags);
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
            cashFlowDetailsState.setType(CashFlow.Type.EXPENSE);
        } else if (CashFlow.Type.EXPENSE.equals(cashFlowDetailsState.getType())) {
            cashFlowDetailsState.setType(CashFlow.Type.INCOME);
        }
        setupTypeDependentViews();
    }

    @OnItemSelected(R.id.wallet)
    void onWalletItemSelected(int position) {
        Wallet selected = (Wallet) wallet.getItemAtPosition(position);
        cashFlowDetailsState.setWallet(selected);
    }

    @OnTextChanged(value = R.id.tag, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAfterTagChanged(Editable s) {
        if (!cashFlowDetailsState.getTags().equals(tag.getText().toString())) {
            resetTagSpans(s);
            cashFlowDetailsState.setCategories(s.toString());
        }
    }

    private void resetTagSpans(Editable s) {
        ImageSpan[] spans = s.getSpans(0, s.length(), ImageSpan.class);
        for (int i = 0; i < spans.length; i++) {
            s.removeSpan(spans[i]);
        }

        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
                if (i - 1 >= start) {
                    String tagName = s.subSequence(start, i).toString();
                    Integer color = cashFlowDetailsState.getTagToColorMap().get(tagName);
                    if (color == null) {
                        Tag tag = tagService.findByName(tagName);
                        color = tag != null
                                ? tag.getColor()
                                : prefUtils.getNextTagColor();
                        cashFlowDetailsState.getTagToColorMap().put(tagName, color);
                    }

                    ImageSpan imageSpan = new ImageSpan(createTagDrawable(tagName, color));
                    s.setSpan(imageSpan, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                start = i + 1;
            }
        }
    }

    private BitmapDrawable createTagDrawable(String tagName, Integer color) {
        //creating textview dynamically
        final TextView tv = new TextView(getActivity());
        tv.setText(tagName);
        tv.setTextSize(getResources().getDimension(R.dimen.mediumFontSize));
        Drawable drawable = getResources().getDrawable(R.drawable.oval);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC);
        tv.setBackground(drawable);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(dipToPixels(getActivity(), 15), 0, dipToPixels(getActivity(), 15), dipToPixels(getActivity(), 1));

        // Convert View to Drawable
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv.measure(spec, spec);
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(tv.getMeasuredWidth(), tv.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-tv.getScrollX(), -tv.getScrollY());
        tv.draw(c);
        tv.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = tv.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        tv.destroyDrawingCache();

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

        } else if (cashFlowDetailsState.getType() == CashFlow.Type.EXPENSE) {
            typeToggle.setText(getString(R.string.expense));
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
        } else if (cashFlowDetailsState.getPreviousType() == CashFlow.Type.EXPENSE) {
            return getString(R.string.expense);
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
                cashFlowService.insert(cashFlowDetailsState.buildCashFlow());
            } else if (DetailsAction.EDIT.equals(detailsAction)) {
                cashFlowService.update(cashFlowDetailsState.buildCashFlow());
            }
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

    private enum DetailsAction {ADD, EDIT}

    public static class WalletAdapter extends BaseAdapter {
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
                textView.setTextSize(context.get().getResources().getDimension(R.dimen.xSmallFontSize));
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

    private class TagAdapter extends BaseAdapter implements Filterable {

        private List<Tag> tags;
        private List<Tag> filtered;

        public TagAdapter(List<Tag> tags) {
            this.tags = tags;
            this.filtered = tags;
        }

        @Override
        public int getCount() {
            return filtered.size();
        }

        @Override
        public Tag getItem(int position) {
            return filtered.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Boolean[] tagClicked = {false};
            final TextView tv = new TextView(getActivity()) {
                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    tagClicked[0] = true;
                    return false;
                }
            };
            tv.setText(getItem(position).getName());
            tv.setTextSize(getActivity().getResources().getDimension(R.dimen.xSmallFontSize));
            Drawable drawable = getActivity().getResources().getDrawable(R.drawable.oval);
            drawable.setColorFilter(getItem(position).getColor(), PorterDuff.Mode.SRC);
            tv.setBackground(drawable);
            tv.setTextColor(Color.WHITE);
            tv.setPadding(dipToPixels(getActivity(), 15), 0, dipToPixels(getActivity(), 15), dipToPixels(getActivity(), 1));
            tv.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            tv.setLayoutParams(layoutParams);


            FrameLayout view = new FrameLayout(getActivity()) {
                public static final float SCROLL_THRESHOLD = 5;
                boolean isOnClick;
                float mDownX;
                float mDownY;

                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            if (!tagClicked[0] && isOnClick) {
                                tag.dismissDropDown();
                                return true;
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                                isOnClick = false;
                            }
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            };
            view.addView(tv);
            return view;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(final CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        filterResults.values = tags;
                        filterResults.count = tags.size();
                    } else {
                        List<Tag> filtered = Lists.newArrayList(Collections2.filter(tags, new Predicate<Tag>() {
                            @Override
                            public boolean apply(Tag input) {
                                return input.getName().toLowerCase().startsWith(constraint.toString().toLowerCase());
                            }
                        }));
                        filterResults.count = filtered.size();
                        filterResults.values = filtered;
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results.count == 0) {
                        notifyDataSetInvalidated();
                    } else {
                        filtered = (List<Tag>) results.values;
                        notifyDataSetChanged();
                    }
                }

                @Override
                public CharSequence convertResultToString(Object tag) {
                    return ((Tag) tag).getName();
                }
            };
        }
    }

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
}
