package info.korzeniowski.walletplus;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
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

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.ui.DrawerListAdapter;
import info.korzeniowski.walletplus.ui.MainDrawerItem;

public class MainActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.drawer)
    ListView drawer;

    @Inject
    DrawerListAdapter drawerListAdapter;

    private ActionBarDrawerToggle drawerToggle;
    private CharSequence appName;
    private CharSequence fragmentTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((WalletPlus) getApplication()).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setupViews();
    }

    void setupViews() {
        initMemberVariables();

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(drawerToggle);

        drawer.setAdapter(drawerListAdapter);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        drawerItemClicked(0);

        drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerItemClicked(position);
            }
        });
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleDrawer();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    void drawerItemClicked(int position) {
        MainDrawerItem selectedMainDrawerItem = (MainDrawerItem) drawer.getAdapter().getItem(position);
        setContentFragment(selectedMainDrawerItem.getFragment(), false);
        drawer.setItemChecked(position, true);
        fragmentTitle = selectedMainDrawerItem.getTitle();
        drawerLayout.closeDrawer(drawer);
    }

    public void setContentFragment(Fragment fragment, Boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        } else {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fragmentTransaction.commit();
    }

    private void initMemberVariables() {
        appName = getString(R.string.appName);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.main_drawer_open,
                R.string.main_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                setTitle(appName);
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                setTitle(fragmentTitle);
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
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
        drawerToggle.setDrawerIndicatorEnabled(isTopFragment());
    }

    private boolean isTopFragment() {
        return getSupportFragmentManager().getBackStackEntryCount() == 0;
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }
}
