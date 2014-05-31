package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cashFlowRowView = inflater.inflate(R.layout.cashflow_list_item, parent, false);
        TextView fromWallet = (TextView) cashFlowRowView.findViewById(R.id.fromWallet);
        TextView toWallet = (TextView) cashFlowRowView.findViewById(R.id.toWallet);
        TextView amount = (TextView) cashFlowRowView.findViewById(R.id.amount);
        TextView category = (TextView) cashFlowRowView.findViewById(R.id.category);
        TextView comment = (TextView) cashFlowRowView.findViewById(R.id.comment);
        TextView date = (TextView) cashFlowRowView.findViewById(R.id.date);

        CashFlow selectedItem = getItem(position);
        amount.setText(new DecimalFormat(",####.00").format(selectedItem.getAmount()));
        comment.setText(selectedItem.getComment());
        date.setText(new SimpleDateFormat("dd/MM/yyyy").format(selectedItem.getDateTime()));

        if (selectedItem.getFromWallet() != null) {
            fromWallet.setText(selectedItem.getFromWallet().getName());
        }
        if (selectedItem.getToWallet() != null) {
            toWallet.setText(selectedItem.getToWallet().getName());
        }
        if (selectedItem.getCategory() != null) {
            category.setText(selectedItem.getCategory().getName());
        }

        return cashFlowRowView;
    }
}
