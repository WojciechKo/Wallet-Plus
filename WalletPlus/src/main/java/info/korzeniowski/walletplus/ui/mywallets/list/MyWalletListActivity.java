package info.korzeniowski.walletplus.ui.mywallets.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.ui.mywallets.details.MyWalletDetailsActivity;

public class MyWalletListActivity extends BaseActivity {
    public static final String TAG = MyWalletListActivity.class.getSimpleName();

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
                    .replace(R.id.container, new MyWalletListFragment())
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
            getSupportFragmentManager().findFragmentById(R.id.container).startActivityForResult(new Intent(this, MyWalletDetailsActivity.class), MyWalletDetailsActivity.REQUEST_CODE_ADD_WALLET);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.MY_WALLETS;
    }
}
