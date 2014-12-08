package info.korzeniowski.walletplus.ui.cashflow.list;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.widget.IdentifiableListAdapter;

public class CashFlowListAdapter extends IdentifiableListAdapter<CashFlow> {

    public CashFlowListAdapter(Context context, List<CashFlow> casFlows) {
        super(context, casFlows, R.layout.cashflow_list_item);
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        CashFlowViewHolder holder = new CashFlowViewHolder();
        ButterKnife.inject(holder, convertView);
        return holder;
    }

    @Override
    protected void fillViewWithItem(MyBaseViewHolder baseHolder, CashFlow item) {
        CashFlowViewHolder holder = (CashFlowViewHolder) baseHolder;
        holder.category.setText(getCategoryText(item));
        holder.fromWallet.setText(getFromWalletText(item));
        holder.toWallet.setText(getToWalletText(item));
        holder.amount.setText(NumberFormat.getCurrencyInstance().format(item.getAmount()));
        holder.amount.setTextColor(getAmountColor(item));
        holder.date.setText(getDateText(item));

        if (Strings.isNullOrEmpty(item.getComment())) {
            holder.comment.setVisibility(View.GONE);
        } else {
            holder.comment.setVisibility(View.VISIBLE);
            holder.comment.setText(getLabeledSpannable(getContext().getString(R.string.cashflowListCommentLabel), item.getComment()));
        }
    }

    private String getCategoryText(CashFlow cashFlow) {
        if (cashFlow.getCategory() == null) {
            return getContext().getResources().getString(R.string.categoryNoCategoryName);
        }
        if (cashFlow.getCategory().getParent() == null) {
            return cashFlow.getCategory().getName();
        }
        return cashFlow.getCategory().getName() + " (" + cashFlow.getCategory().getParent().getName() + ")";
    }

    private CharSequence getFromWalletText(CashFlow item) {
        if (item.getFromWallet() != null) {
            return getLabeledSpannable(getContext().getString(R.string.cashflowListFromWalletLabel), item.getFromWallet().getName());
        } else {
            return getContext().getString(R.string.cashflowListFromWalletLabel);
        }
    }

    private CharSequence getToWalletText(CashFlow item) {
        if (item.getToWallet() != null) {
            return getLabeledSpannable(getContext().getString(R.string.cashflowListToWalletLabel), item.getToWallet().getName());
        } else {
            return getContext().getString(R.string.cashflowListToWalletLabel);
        }
    }

    private int getAmountColor(CashFlow item) {
        CashFlow.Type type = item.getType();

        if (type == CashFlow.Type.EXPANSE) {
            return getContext().getResources().getColor(R.color.red);
        } else if (type == CashFlow.Type.INCOME) {
            return getContext().getResources().getColor(R.color.green);
        } else if (type == CashFlow.Type.TRANSFER) {
            return getContext().getResources().getColor(R.color.blue);
        }
        return getContext().getResources().getColor(R.color.black);
    }

    private String getDateText(CashFlow item) {
        String timeString = DateFormat.getTimeFormat(getContext()).format(item.getDateTime());
        String dateString = DateFormat.getDateFormat(getContext()).format(item.getDateTime());
        return timeString + "\n" + dateString;
    }

    private CharSequence getLabeledSpannable(String label, String text) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(label + " " + text);
        spanTxt.setSpan(new AbsoluteSizeSpan((int) getContext().getResources().getDimension(R.dimen.mediumFontSize)), spanTxt.length() - text.length(), spanTxt.length(), 0);
        return spanTxt;
    }

    class CashFlowViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.fromWallet)
        protected TextView fromWallet;

        @InjectView(R.id.toWallet)
        protected TextView toWallet;

        @InjectView(R.id.amount)
        protected TextView amount;

        @InjectView(R.id.category)
        protected TextView category;

        @InjectView(R.id.comment)
        protected TextView comment;

        @InjectView(R.id.date)
        protected TextView date;
    }
}
