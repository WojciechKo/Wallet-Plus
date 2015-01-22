package info.korzeniowski.walletplus.ui.otherwallets.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.ui.otherwallets.details.OtherWalletDetailsActivity;

public class OtherWalletListActivity extends BaseActivity {
    public static final String TAG = OtherWalletListActivity.class.getSimpleName();

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
                    .replace(R.id.container, new OtherWalletListFragment())
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
            getSupportFragmentManager().findFragmentById(R.id.container).startActivityForResult(new Intent(this, OtherWalletDetailsActivity.class), OtherWalletDetailsActivity.REQUEST_CODE_ADD_WALLET);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.OTHER_WALLETS;
    }
}
