package info.korzeniowski.walletplus.ui.wallet.list;

import android.content.Context;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Wallet;

import static android.view.View.OnClickListener;

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.WalletViewHolder> {

    private List<Wallet> myWallets;
    private WeakReference<Context> context;
    private WeakReference<OnClickListener> onClickListener;

    public WalletListAdapter(Context context, List<Wallet> myWallets, OnClickListener onClickListener) {
        this.myWallets = myWallets;
        this.context = new WeakReference<Context>(context);
        this.onClickListener = new WeakReference<OnClickListener>(onClickListener);
    }

    @Override
    public WalletViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_item_list, parent, false);
        view.setOnClickListener(onClickListener.get());
        return new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WalletViewHolder walletViewHolder, int position) {
        Wallet item = myWallets.get(position);
        walletViewHolder.walletName.setText(item.getName());
        walletViewHolder.currentAmount.setText(NumberFormat.getCurrencyInstance().format(item.getCurrentAmount()));
        if (item.getCurrentAmount() < 0) {
            walletViewHolder.currentAmount.setTextColor(context.get().getResources().getColor(R.color.red));
        } else {
            walletViewHolder.currentAmount.setTextColor(context.get().getResources().getColor(R.color.green));
        }
    }

    @Override
    public int getItemCount() {
        return myWallets.size();
    }

    @Override
    public long getItemId(int position) {
        return myWallets.get(position).getId();
    }

    public static class WalletViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.walletName)
        protected TextView walletName;

        @InjectView(R.id.currentAmount)
        protected TextView currentAmount;

        WalletViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
