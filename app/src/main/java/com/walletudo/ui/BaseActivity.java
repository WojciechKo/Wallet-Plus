package com.walletudo.ui;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.walletudo.DatabaseInitializer;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.dagger.AppComponent;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.ProfileService;
import com.walletudo.service.StatisticService;
import com.walletudo.service.TagService;
import com.walletudo.service.WalletService;
import com.walletudo.ui.dashboard.DashboardActivity;
import com.walletudo.util.PrefUtils;
import com.walletudo.util.RequestCode;

import javax.inject.Inject;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int MAIN_CONTENT_FADE_IN_DURATION = 250;

    @Inject
    protected ProfileService profileService;

    @Inject
    protected StatisticService statisticService;

    @Inject
    protected WalletService walletService;

    @Inject
    protected TagService tagService;

    @Inject
    protected CashFlowService cashFlowService;

    @Inject
    PrefUtils prefUtils;

    private Toolbar toolbar;

    private NavigationDrawerHelper navigationDrawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppComponent appComponent = ((Walletudo) getApplication()).component();
        PrefUtils prefUtils = appComponent.prefUtils();
        // Perform one-time bootstrap setup, if needed
        if (!prefUtils.isDataBootstrapDone()) {
            Log.d(TAG, "One-time data bootstrap not done yet. Doing now.");
            performDataBootstrap();
        }
        appComponent.inject(this);
        navigationDrawerHelper = NavigationDrawerHelper.attach(this);
        openDrawerOnWelcome();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null && navigationDrawerHelper.isDrawerPresent()) {
            toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigationDrawerHelper.openNavDrawer();
                }
            });
        }
    }

    private void performDataBootstrap() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Starting data bootstrap process.");

                new DatabaseInitializer(BaseActivity.this).createExampleAccountWithProfile();
                prefUtils.markDataBootstrapDone();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                BaseActivity.this.recreate();
            }
        }.execute();
    }

    private void openDrawerOnWelcome() {
        if (!prefUtils.isWelcomeDone()) {
            prefUtils.markWelcomeDone();
            navigationDrawerHelper.openNavDrawer();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADE_IN_DURATION);
        } else {
            Log.w(TAG, "No view with ID main_content to fade in.");
        }
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawerHelper.isNavDrawerOpen()) {
            navigationDrawerHelper.closeNavDrawer();
        } else {
            Intent parentIntent = NavUtils.getParentActivityIntent(this);
            if (parentIntent != null) {
                navigateToParent(parentIntent);
            } else {
                super.onBackPressed();
            }
        }
    }

    private void navigateToParent(Intent parentIntent) {
        if ((DashboardActivity.class.getName().equals(parentIntent.getComponent().getClassName())
                || NavUtils.shouldUpRecreateTask(this, parentIntent))) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(parentIntent)
                    .startActivities();
        } else {
            NavUtils.navigateUpTo(this, parentIntent);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (navigationDrawerHelper.isNavDrawerOpen()) {
                navigationDrawerHelper.closeNavDrawer();
            } else {
                navigationDrawerHelper.openNavDrawer();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = null;
        getActionBarToolbar();
    }

    public Toolbar getActionBarToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        return toolbar;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.NEW_PROFILE_RC:
                if (resultCode == RESULT_OK) {
                    navigationDrawerHelper.selectProfileById(profileService.getActiveProfile().getId());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected NavigationDrawerHelper.DrawerItemType getSelfNavDrawerItem() {
        return NavigationDrawerHelper.DrawerItemType.INVALID;
    }
}
