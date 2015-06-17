package com.walletudo.ui.wallets.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.walletudo.R;
import com.walletudo.ui.BaseActivity;
import com.walletudo.ui.NavigationDrawerHelper;
import com.walletudo.ui.wallets.details.WalletDetailsActivity;

public class WalletListActivity extends BaseActivity {
    public static final String TAG = WalletListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_drawer);

        if (null == savedInstanceState) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new WalletListFragment())
                    .commit();
        }

        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            Intent walletDetailsIntent = new Intent(this, WalletDetailsActivity.class);
            getSupportFragmentManager()
                    .findFragmentById(R.id.container)
                    .startActivityForResult(walletDetailsIntent, WalletDetailsActivity.REQUEST_CODE_ADD_WALLET);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected NavigationDrawerHelper.DrawerItemType getSelfNavDrawerItem() {
        return NavigationDrawerHelper.DrawerItemType.WALLET;
    }
}
