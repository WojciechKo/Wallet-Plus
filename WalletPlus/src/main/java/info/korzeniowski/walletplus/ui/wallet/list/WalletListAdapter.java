package info.korzeniowski.walletplus.ui.wallet.list;

import android.content.Context;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.widget.IdentifiableListAdapter;

import static android.view.View.OnClickListener;

public class WalletListAdapter extends IdentifiableListAdapter<Wallet> {

    private WeakReference<OnClickListener> onClickListener;

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
    protected void fillViewWithItem(MyBaseViewHolder holder, Wallet item) {
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
        protected TextView walletName;

        @InjectView(R.id.currentAmount)
        protected TextView currentAmount;
    }
}
