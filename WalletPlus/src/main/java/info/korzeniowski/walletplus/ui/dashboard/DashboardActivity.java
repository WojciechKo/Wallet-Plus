package info.korzeniowski.walletplus.ui.dashboard;

import android.os.Bundle;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.BaseActivity;

public class DashboardActivity extends BaseActivity {

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
                    .replace(R.id.container, new DashboardFragment())
                    .commit();
        }

        overridePendingTransition(0, 0);
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.DASHBOARD;
    }
}
