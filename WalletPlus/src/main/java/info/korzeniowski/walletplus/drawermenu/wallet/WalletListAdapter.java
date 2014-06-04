package info.korzeniowski.walletplus.drawermenu.wallet;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Wallet;

public class WalletListAdapter extends BaseAdapter {
    private List<Wallet> myWallets;
    private Context context;

    public WalletListAdapter(FragmentActivity activity, List<Wallet> myWallets) {
        this.myWallets = myWallets;
        this.context = activity;
    }

    @Override
    public int getCount() {
        return myWallets.size();
    }

    @Override
    public Wallet getItem(int position) {
        return myWallets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.wallet_list_item, parent, false);
        }
        Wallet wallet = getItem(position);

        TextView walletName = (TextView) convertView.findViewById(R.id.walletName);
        walletName.setText(wallet.getName());

        TextView initialAmount = (TextView) convertView.findViewById(R.id.initialAmount);
        initialAmount.setText(context.getString(R.string.initialValue) + " " + wallet.getInitialAmount());

        TextView actualAmount = (TextView) convertView.findViewById(R.id.actualAmount);
        actualAmount.setText(wallet.getInitialAmount().toString());

        return convertView;
    }
}
