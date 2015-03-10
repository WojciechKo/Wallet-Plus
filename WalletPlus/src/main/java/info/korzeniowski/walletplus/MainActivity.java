package info.korzeniowski.walletplus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.service.ormlite.AccountServiceOrmLite;
import info.korzeniowski.walletplus.ui.drawer.DrawerAccountAdapter;
import info.korzeniowski.walletplus.widget.SquareImageButton;

public class MainActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.drawer)
    LinearLayout drawer;

    @InjectView(R.id.accountName)
    TextView selectedAccount;

    @InjectView(R.id.switchAccount)
    SquareImageButton switchAccount;

    @InjectView(R.id.drawerList)
    ListView drawerList;

    @Inject
    AccountServiceOrmLite accountServiceOrmLite;

    private DrawerAccountAdapter drawerAccountAdapter;
    private View accountListFooter;
    private MainActivityParcelableState state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((WalletPlus) getApplication()).component().inject(this);
        ButterKnife.inject(this);
        restoreOrInitState(savedInstanceState);
        setupViews();
    }

    private void restoreOrInitState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            state = savedInstanceState.getParcelable(MainActivityParcelableState.TAG);
        } else {
            initMainActivityState();
        }
    }

    private void initMainActivityState() {
        state = new MainActivityParcelableState();
        state.setSelectedDrawerPosition(0);
    }

    void setupViews() {
//        drawerMenuAdapter = new DrawerMenuAdapter(this, mainDrawerContent);
        drawerAccountAdapter = getDrawerAccountAdapter();
        accountListFooter = getNewAccountFooter();

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        setupDrawerListAsMenu();
        setupAccountFrame();
        setupToolbar();
        setupContentFrame();

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    private DrawerAccountAdapter getDrawerAccountAdapter() {
        return new DrawerAccountAdapter(this, accountServiceOrmLite.getAll());
    }

    private View getNewAccountFooter() {
        TextView accountListFooter = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        accountListFooter.setText(getString(R.string.newAccount));
        accountListFooter.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_plus_grey600_24dp), null, null, null);
        accountListFooter.setBackgroundResource(R.drawable.selector_list_item);
        accountListFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(MainActivity.this);

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Enter new account name")
                        .setView(input)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                accountServiceOrmLite.insert(new Account().setName(input.getText().toString()));
                                drawerAccountAdapter = getDrawerAccountAdapter();
                                drawerList.setAdapter(drawerAccountAdapter);
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
            }
        });
        return accountListFooter;
    }

    private void setupDrawerListAsMenu() {
//        drawerList.setAdapter(drawerMenuAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerMenuClicked(position);
            }
        });

        if (drawerList.getFooterViewsCount() != 0) {
            drawerList.removeFooterView(accountListFooter);
        }
    }

    private void setupAccountFrame() {
//        ProfileUtils.getActiveProfileId(this)
//        selectedAccount.setText(((WalletPlus) getApplication()).getCurrentProfile().getName());

        switchAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchAccount.isChecked()) {
                    setupDrawerListAsAccountList();
                } else {
                    setupDrawerListAsMenu();
                }
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_white_24dp));
    }

    private void setupContentFrame() {
        if (state.getFragmentTag() != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment lastFragment = getSupportFragmentManager().findFragmentByTag(state.getFragmentTag());
            fragmentTransaction.replace(R.id.content_frame, lastFragment, state.getFragmentTag());
            fragmentTransaction.commit();
        } else {
            setSelectedDrawerItem(state.getSelectedDrawerPosition());
        }
    }

    private void setupDrawerListAsAccountList() {
        drawerList.setAdapter(drawerAccountAdapter);
        drawerList.addFooterView(accountListFooter, null, true);
        drawerList.setFooterDividersEnabled(true);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerAccountClicked(position);
            }
        });
    }

    private void drawerAccountClicked(int position) {
        drawerLayout.closeDrawer(drawer);
//        ((WalletPlus) getApplication()).setCurrentProfile(drawerAccountAdapter.getItem(position));
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ((WalletPlus) getApplication()).reinitializeObjectGraph();
        initMainActivityState();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MainActivityParcelableState.TAG, state);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((Build.VERSION.SDK_INT < 5) && (keyCode == KeyEvent.KEYCODE_BACK)) && (event.getRepeatCount() == 0)) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        } else if (isTopFragment() && state.getSelectedDrawerPosition() != 0) {
            drawerMenuClicked(0);
        } else {
            super.onBackPressed();
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

    void drawerMenuClicked(int position) {
        if (state.getSelectedDrawerPosition() != position) {
            setSelectedDrawerItem(position);
        }
        drawerLayout.closeDrawer(drawer);
    }

    private void setSelectedDrawerItem(int position) {
//        drawerMenuAdapter.setSelected(state.getSelectedDrawerPosition());
//        MainDrawerItem selectedMainDrawerItem = drawerMenuAdapter.getItem(position);
//        setContentFragment(selectedMainDrawerItem.getFragment(), false, selectedMainDrawerItem.getTag());
//        setTitle(selectedMainDrawerItem.getTitle());
//
//        state.setSelectedDrawerPosition(position);
//        state.setSelectedFragmentTitle(selectedMainDrawerItem.getTitle());
//        state.setFragmentTag(selectedMainDrawerItem.getTag());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            homeClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void homeClicked() {
        if (isTopFragment()) {
            toggleDrawer();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        } else {
            drawerLayout.openDrawer(drawer);
        }
    }

    @Override
    public void onBackStackChanged() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        state.setFragmentTag(fragment.getTag());
        if (isTopFragment()) {
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_white_24dp));
        } else {
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_left_white_24dp));
        }
    }

    private boolean isTopFragment() {
        return getSupportFragmentManager().getBackStackEntryCount() == 0;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }
}
