package info.korzeniowski.walletplus.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.widget.IdentifiableListAdapter;

public class DrawerAccountAdapter extends IdentifiableListAdapter<Account> {

    public DrawerAccountAdapter(Context context, List<Account> accountList) {
        super(context, accountList, android.R.layout.simple_list_item_1);
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        AccountViewHolder holder = new AccountViewHolder();
        ButterKnife.inject(holder, convertView);
        return holder;
    }

    @Override
    protected void fillViewWithItem(MyBaseViewHolder baseHolder, Account account) {
        AccountViewHolder holder = (AccountViewHolder) baseHolder;
        holder.name.setText(account.getName());
    }

    static class AccountViewHolder extends MyBaseViewHolder {
        @InjectView(android.R.id.text1)
        TextView name;
    }
}
