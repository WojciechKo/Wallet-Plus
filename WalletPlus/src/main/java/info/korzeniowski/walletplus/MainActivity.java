package info.korzeniowski.walletplus;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.ui.DrawerListAdapter;
import info.korzeniowski.walletplus.ui.MainDrawerItem;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsStateListener;

public class MainActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener, CashFlowDetailsStateListenerManager {

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.drawer)
    ListView drawer;

    @Inject
    DrawerListAdapter drawerListAdapter;

    private ActionBarDrawerToggle drawerToggle;
    private MainActivityParcelableState state;
    private List<CashFlowDetailsStateListener> cashFlowDetailsStateListeners;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((WalletPlus) getApplication()).inject(this);
        ButterKnife.inject(this);
        restoreOrInitState(savedInstanceState);
        setupViews();
    }

    private void restoreOrInitState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            state = savedInstanceState.getParcelable(MainActivityParcelableState.TAG);
        } else {
            state = new MainActivityParcelableState();
            state.setAppName(getString(R.string.appName));
            state.setSelectedDrawerPosition(0);
        }
        drawerToggle = new MainActivityDrawerToggle(this);
        cashFlowDetailsStateListeners = Lists.newArrayList();
    }

    void setupViews() {
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(drawerToggle);

        drawer.setAdapter(drawerListAdapter);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (state.getFragmentTag() != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment lastFragment = getSupportFragmentManager().findFragmentByTag(state.getFragmentTag());
            fragmentTransaction.replace(R.id.content_frame, lastFragment, state.getFragmentTag());
            fragmentTransaction.commit();
        } else {
            drawerItemClicked(state.getSelectedDrawerPosition());
        }

        drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                state.setSelectedDrawerPosition(position);
                drawerItemClicked(position);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MainActivityParcelableState.TAG, state);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == android.R.id.home) {
            homeSelected(item);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((Build.VERSION.SDK_INT < 5) && (keyCode == KeyEvent.KEYCODE_BACK)) && (event.getRepeatCount() == 0)) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void homeSelected(MenuItem item) {
        if (!drawerToggle.onOptionsItemSelected(item)) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleDrawer();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    void drawerItemClicked(int position) {
        MainDrawerItem selectedMainDrawerItem = (MainDrawerItem) drawer.getAdapter().getItem(position);
        setContentFragment(selectedMainDrawerItem.getFragment(), false, selectedMainDrawerItem.getTag());
        drawer.setItemChecked(position, true);
        state.setSelectedFragmentTitle(selectedMainDrawerItem.getTitle());
        state.setFragmentTag(selectedMainDrawerItem.getTag());
        drawerLayout.closeDrawer(drawer);
    }

    public void setContentFragment(Fragment fragment, Boolean addToBackStack, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, tag);
        state.setFragmentTag(tag);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        } else {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fragmentTransaction.commit();
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        } else {
            drawerLayout.openDrawer(drawer);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (drawerLayout.isDrawerOpen(drawer)) {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackStackChanged() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        state.setFragmentTag(fragment.getTag());
        drawerToggle.setDrawerIndicatorEnabled(isTopFragment());
    }

    private boolean isTopFragment() {
        return getSupportFragmentManager().getBackStackEntryCount() == 0;
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        drawerToggle = null;
        super.onDestroy();
    }

    @Override
    public void addCashFlowDetailsStateListener(CashFlowDetailsStateListener fragment) {
        this.cashFlowDetailsStateListeners.add(fragment);
    }

    @Override
    public void removeCashFlowDetailsStateListener(CashFlowDetailsStateListener fragment) {
        this.cashFlowDetailsStateListeners.remove(fragment);
    }

    @Override
    public void cashFlowStateChanged(CashFlowDetailsStateListener notifierFragment) {
        for (CashFlowDetailsStateListener listener : cashFlowDetailsStateListeners) {
            if (listener != notifierFragment) {
                listener.update();
            }
        }
    }

    private class MainActivityDrawerToggle extends ActionBarDrawerToggle {
        MainActivityDrawerToggle(Activity activity) {
            super(activity,
                    drawerLayout,
                    R.drawable.ic_drawer,
                    R.string.main_drawer_open,
                    R.string.main_drawer_close);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            setTitle(state.getAppName());
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            setTitle(state.getSelectedFragmentTitle());
            super.onDrawerClosed(drawerView);
            invalidateOptionsMenu();
        }
    }
}
