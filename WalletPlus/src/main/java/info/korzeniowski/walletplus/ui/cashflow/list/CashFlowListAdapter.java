package info.korzeniowski.walletplus.ui.cashflow.list;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.widget.IdentifiableListAdapter;

public class CashFlowListAdapter extends IdentifiableListAdapter<CashFlow> {

    public CashFlowListAdapter(Context context, List<CashFlow> casFlows) {
        super(context, casFlows, R.layout.item_cash_flow_list);
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
        holder.wallet.setText(getWalletText(item));
        holder.amount.setText(NumberFormat.getCurrencyInstance().format(item.getAmount()));
        holder.amount.setTextColor(getAmountColor(item));
        holder.date.setText(getDateText(item));
    }

    private String getCategoryText(CashFlow cashFlow) {
        StringBuilder sb = new StringBuilder();
        Iterator<Category> iterator = cashFlow.getCategories().iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next().getName());
        }
        while (iterator.hasNext()) {
            sb.append(" ").append(iterator.next().getName());
        }
        return sb.toString();
    }

    private CharSequence getWalletText(CashFlow item) {
        return item.getWallet().getName();
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
        return dateString + " " + timeString;
    }

    private CharSequence getLabeledSpannable(String label, String text) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(label + " " + text);
        spanTxt.setSpan(new AbsoluteSizeSpan((int) getContext().getResources().getDimension(R.dimen.mediumFontSize)), spanTxt.length() - text.length(), spanTxt.length(), 0);
        return spanTxt;
    }

    class CashFlowViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.wallet)
        TextView wallet;

        @InjectView(R.id.amount)
        TextView amount;

        @InjectView(R.id.category)
        TextView category;

        @InjectView(R.id.date)
        TextView date;
    }
}
