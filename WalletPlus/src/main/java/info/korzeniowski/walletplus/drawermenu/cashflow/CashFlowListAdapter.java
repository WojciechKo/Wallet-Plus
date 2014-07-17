package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.text.DecimalFormat;
import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.CashFlow;

public class CashFlowListAdapter extends ArrayAdapter<CashFlow> {

    public CashFlowListAdapter(Context context, List<CashFlow> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cashflow_list_item, null);
            holder = createHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        fillViewWithItem(holder, getItem(position));

        return convertView;
    }

    private ViewHolder createHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.fromWallet = (TextView) convertView.findViewById(R.id.fromWallet);
        holder.toWallet = (TextView) convertView.findViewById(R.id.toWallet);
        holder.amount = (TextView) convertView.findViewById(R.id.amount);
        holder.category = (TextView) convertView.findViewById(R.id.category);
        holder.commentLabel = (TextView) convertView.findViewById(R.id.commentLabel);
        holder.comment = (TextView) convertView.findViewById(R.id.comment);
        holder.date = (TextView) convertView.findViewById(R.id.date);
        return holder;
    }

    private void fillViewWithItem(ViewHolder holder, CashFlow item) {

        if (Strings.isNullOrEmpty(item.getComment())) {
            holder.commentLabel.setVisibility(View.GONE);
            holder.comment.setVisibility(View.GONE);
        } else {
            holder.commentLabel.setVisibility(View.VISIBLE);
            holder.comment.setVisibility(View.VISIBLE);
            holder.comment.setText(item.getComment());
        }

        holder.amount.setText(new DecimalFormat(getContext().getString(R.string.amountFormat)).format(item.getAmount()));

        if (item.isExpanse()) {
            holder.amount.setTextColor(getContext().getResources().getColor(R.color.red));
        } else if (item.isIncome()) {
            holder.amount.setTextColor(getContext().getResources().getColor(R.color.green));
        } else if (item.isTransfer()) {
            holder.amount.setTextColor(getContext().getResources().getColor(R.color.blue));
        }

        String timeString = DateFormat.getTimeFormat(getContext()).format(item.getDateTime());
        String dateString = DateFormat.getDateFormat(getContext()).format(item.getDateTime());
        holder.date.setText(timeString + "\n" + dateString);

        if (item.getFromWallet() != null) {
            holder.fromWallet.setText(item.getFromWallet().getName());
        } else {
            holder.fromWallet.setText("");
        }
        if (item.getToWallet() != null) {
            holder.toWallet.setText(item.getToWallet().getName());
        } else {
            holder.toWallet.setText("");
        }
        if (item.getCategory() != null) {
            holder.category.setText(item.getCategory().getName());
        } else {
            holder.category.setText("Other");
        }

    }

    private class ViewHolder {
        protected TextView fromWallet;
        protected TextView toWallet;
        protected TextView amount;
        protected TextView commentLabel;
        protected TextView category;
        protected TextView comment;
        protected TextView date;
    }
}
