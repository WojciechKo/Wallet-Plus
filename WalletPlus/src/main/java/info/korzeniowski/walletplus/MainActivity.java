package info.korzeniowski.walletplus;

import android.content.res.Configuration;
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
import android.widget.ListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;

import info.korzeniowski.walletplus.drawermenu.DrawerListAdapter;
import info.korzeniowski.walletplus.drawermenu.MainDrawerItem;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {

    @ViewById
    DrawerLayout drawerLayout;

    @ViewById
    ListView drawer;

    @Inject
    DrawerListAdapter drawerListAdapter;

    private ActionBarDrawerToggle drawerToggle;
    private CharSequence appName;
    private CharSequence fragmentTitle;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getApplication()).inject(this);
    }

    @AfterViews
    void setupViews() {
        initMemberVariables();

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(drawerToggle);

        drawer.setAdapter(drawerListAdapter);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        drawerItemClicked(0);
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

    @OptionsItem(android.R.id.home)
    void homeSelected(MenuItem item) {
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

    @ItemClick
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
