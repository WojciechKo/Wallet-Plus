package info.korzeniowski.walletplus.activity;

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
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.ViewById;

import javax.inject.Inject;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.drawermenu.MainDrawerListAdapter;
import info.korzeniowski.walletplus.drawermenu.MainDrawerContent;
import info.korzeniowski.walletplus.drawermenu.MainDrawerItem;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity{

    @App
    WalletPlus application;

    @ViewById
    DrawerLayout drawerLayout;

    @ViewById
    ListView mainDrawer;

    @Inject
    MainDrawerContent mMainDrawerContent;

    @Inject
    MainDrawerListAdapter mMainDrawerListAdapter;

    private ActionBarDrawerToggle mainDrawerToggle;

    private int defaultMainDrawerItemSelected = 0;

    @AfterInject
    void daggerInject() {
        application.inject(this);
    }

    @AfterViews
    void setupViews() {
        initMemberVariables();

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(mainDrawerToggle);

        mainDrawer.setAdapter(mMainDrawerListAdapter);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainDrawerItemClicked(defaultMainDrawerItemSelected);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mainDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mainDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Without (andorid.R.id.home) not working.
     * Don't know why.
     */
    @OptionsItem(android.R.id.home)
    void homeSelected() {
        toggleDrawer(mainDrawer);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleDrawer(mainDrawer);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @ItemClick
    void mainDrawerItemClicked(int position) {
        MainDrawerItem selectedMainDrawerItem = mMainDrawerContent.getDrawerItem(position);
        setContentFragment(selectedMainDrawerItem.getFragment(), false);
        mainDrawer.setItemChecked(position, true);
        drawerLayout.closeDrawer(mainDrawer);
        setActionBarTitle(selectedMainDrawerItem.getTitle());
    }

    public void setContentFragment(Fragment fragment, Boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        if (addToBackStack)
            fragmentTransaction.addToBackStack(null);
        else
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }

    private void initMemberVariables() {
        mainDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.main_drawer_open,
                R.string.main_drawer_close);
    }

    private void toggleDrawer(ListView drawerMenu) {
        if (drawerLayout.isDrawerOpen(drawerMenu)) {
            drawerLayout.closeDrawer(drawerMenu);
        } else {
            drawerLayout.openDrawer(drawerMenu);
        }
    }

    private void setActionBarTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }
}