package pl.net.korzeniowski.walletplus.ui.cashflow.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.ui.BaseActivity;
import pl.net.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsActivity;

public class CashFlowListActivity extends BaseActivity {
    public static final String TAG = CashFlowListActivity.class.getSimpleName();

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
                    .replace(R.id.container, new CashFlowListFragment())
                    .commit();
        }

        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_switch, menu);
        getMenuInflater().inflate(R.menu.action_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            getSupportFragmentManager()
                    .findFragmentById(R.id.container)
                    .startActivityForResult(
                            new Intent(this, CashFlowDetailsActivity.class), CashFlowDetailsActivity.REQUEST_CODE_ADD_CASH_FLOW);
            return true;
        } else if (item.getItemId() == R.id.menu_switch) {
            item.setChecked(!item.isChecked());
            if (item.isChecked()) {
                item.setIcon(R.drawable.ic_toggle_switch);
            } else {
                item.setIcon(R.drawable.ic_toggle_switch_off);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.CASH_FLOW;
    }
}
