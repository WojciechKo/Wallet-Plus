package info.korzeniowski.walletplus.ui.wallet;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.widget.IdentityableListAdapter;

public class WalletListAdapter extends IdentityableListAdapter<Wallet> {

    public WalletListAdapter(Context context, List<Wallet> myWallets) {
        super(context, myWallets, R.layout.wallet_item_list);
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        WalletViewHolder holder = new WalletViewHolder();
        ButterKnife.inject(holder, convertView);
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
        @InjectView(R.id.walletName)
        protected TextView walletName;

        @InjectView(R.id.initialAmount)
        protected TextView initialAmount;

        @InjectView(R.id.currentAmount)
        protected TextView currentAmount;
    }
}
