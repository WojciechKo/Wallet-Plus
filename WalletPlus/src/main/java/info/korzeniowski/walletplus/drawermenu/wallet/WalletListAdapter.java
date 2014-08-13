package info.korzeniowski.walletplus.drawermenu.wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.text.NumberFormat;
import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.widget.IdentityableListAdapter;

public class WalletListAdapter extends IdentityableListAdapter<Wallet> {

    public WalletListAdapter(Context context, List<Wallet> myWallets) {
        super(context, myWallets, R.layout.wallet_item_list);
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        WalletViewHolder holder = new WalletViewHolder();
        holder.walletName = (TextView) convertView.findViewById(R.id.walletName);
        holder.initialAmount = (TextView) convertView.findViewById(R.id.initialAmount);
        holder.currentAmount = (TextView) convertView.findViewById(R.id.currentAmount);
        return holder;
    }

    @Override
    protected void fillViewWithItem(MyBaseViewHolder baseHolder, Wallet item) {
        WalletViewHolder holder = (WalletViewHolder) baseHolder;
        holder.walletName.setText(item.getName());
        holder.initialAmount.setText(getContext().getString(R.string.initialValue) + " " + NumberFormat.getCurrencyInstance().format(item.getInitialAmount()));
        holder.currentAmount.setText(NumberFormat.getCurrencyInstance().format(item.getCurrentAmount()));
        if (item.getCurrentAmount() < 0) {
            holder.currentAmount.setTextColor(getContext().getResources().getColor(R.color.red));
        } else {
            holder.currentAmount.setTextColor(getContext().getResources().getColor(R.color.green));
        }
    }


    class WalletViewHolder extends MyBaseViewHolder {
        protected TextView walletName;
        protected TextView initialAmount;
        protected TextView currentAmount;
    }
}
