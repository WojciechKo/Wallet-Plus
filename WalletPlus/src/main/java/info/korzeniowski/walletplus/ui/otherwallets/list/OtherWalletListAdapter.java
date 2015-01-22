package info.korzeniowski.walletplus.ui.otherwallets.list;

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

public class OtherWalletListAdapter extends IdentifiableListAdapter<Wallet> {

    public OtherWalletListAdapter(Context context, List<Wallet> myWallets) {
        super(context, myWallets, R.layout.item_other_wallet_list);
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
    }

    public class WalletViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.walletName)
        TextView walletName;
    }
}
