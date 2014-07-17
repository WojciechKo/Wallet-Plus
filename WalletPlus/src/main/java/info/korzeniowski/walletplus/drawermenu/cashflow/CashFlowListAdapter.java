package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cashflow_list_item, parent, false);
        }
        TextView fromWallet = (TextView) convertView.findViewById(R.id.fromWallet);
        TextView toWallet = (TextView) convertView.findViewById(R.id.toWallet);
        TextView amount = (TextView) convertView.findViewById(R.id.amount);
        TextView category = (TextView) convertView.findViewById(R.id.category);
        TextView comment = (TextView) convertView.findViewById(R.id.comment);
        TextView date = (TextView) convertView.findViewById(R.id.date);

        CashFlow selectedItem = getItem(position);
        amount.setText(new DecimalFormat(getContext().getString(R.string.amountFormat)).format(selectedItem.getAmount()));

        if (selectedItem.isExpanse()) {
            amount.setTextColor(getContext().getResources().getColor(R.color.red));
        } else if (selectedItem.isIncome()) {
            amount.setTextColor(getContext().getResources().getColor(R.color.green));
        } else if (selectedItem.isTransfer()) {
            amount.setTextColor(getContext().getResources().getColor(R.color.blue));
        }

        comment.setText(selectedItem.getComment());
        String timeString = DateFormat.getTimeFormat(getContext()).format(selectedItem.getDateTime());
        String dateString = DateFormat.getDateFormat(getContext()).format(selectedItem.getDateTime());
        date.setText(timeString + " " + dateString);

        if (selectedItem.getFromWallet() != null) {
            fromWallet.setText(selectedItem.getFromWallet().getName());
        }
        if (selectedItem.getToWallet() != null) {
            toWallet.setText(selectedItem.getToWallet().getName());
        }
        if (selectedItem.getCategory() != null) {
            category.setText(selectedItem.getCategory().getName());
        }

        return convertView;
    }
}
