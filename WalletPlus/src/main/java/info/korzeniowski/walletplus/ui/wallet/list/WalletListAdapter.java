package info.korzeniowski.walletplus.ui.wallet.list;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.widget.IdentifiableListAdapter;

public class WalletListAdapter extends IdentifiableListAdapter<Wallet> {

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
    protected void fillViewWithItem(MyBaseViewHolder holder, final Wallet item) {
        WalletViewHolder walletHolder = (WalletViewHolder) holder;
        walletHolder.walletName.setText(item.getName());
        walletHolder.currentAmount.setText(NumberFormat.getCurrencyInstance().format(item.getCurrentAmount()));
        if (item.getCurrentAmount() < 0) {
            walletHolder.currentAmount.setTextColor(getContext().getResources().getColor(R.color.red));
        } else {
            walletHolder.currentAmount.setTextColor(getContext().getResources().getColor(R.color.green));
        }
    }

    public class WalletViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.walletName)
        TextView walletName;

        @InjectView(R.id.currentAmount)
        TextView currentAmount;

        @InjectView(R.id.deleteButton)
        Button delete;
    }
}
