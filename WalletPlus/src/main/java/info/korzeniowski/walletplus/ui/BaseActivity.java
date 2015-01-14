package info.korzeniowski.walletplus.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.samples.apps.iosched.ui.widget.ScrimInsetsScrollView;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.service.AccountService;
import info.korzeniowski.walletplus.ui.cashflow.list.CashFlowListActivity;
import info.korzeniowski.walletplus.ui.category.list.CategoryListActivity;
import info.korzeniowski.walletplus.ui.category.list.CategoryListActivityState;
import info.korzeniowski.walletplus.ui.dashboard.DashboardActivity;
import info.korzeniowski.walletplus.ui.wallet.list.WalletListActivity;
import info.korzeniowski.walletplus.util.AccountUtils;
import info.korzeniowski.walletplus.util.PrefUtils;
import info.korzeniowski.walletplus.util.UIUtils;


public class BaseActivity extends ActionBarActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    // delay to launch nav drawer type, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;

    private Toolbar mActionBarToolbar;
    private DrawerLayout mDrawerLayout;
    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;
    private boolean mAccountBoxExpanded = false;
    private boolean mActionBarShown = true;
    private LinearLayout mAccountListContainer;
    private ImageView mExpandAccountBoxIndicator;
    private Handler mHandler;
    private int mThemedStatusBarColor;
    private int mNormalStatusBarColor;
    private ViewGroup mDrawerItemsListContainer;
    // views that correspond to each navigation_drawer type, null if not yet created
    private View[] mNavDrawerItemViews = null;
    private Thread mDataBootstrapThread;
    // Navigation drawer menu items
    private Map<DrawerItemType, DrawerItemContent> navigationDrawerMap = Maps.newHashMap();
    private List<DrawerItemType> navigationDrawerItemList = Lists.newArrayList();

    @Inject
    @Named("local")
    AccountService accountService;

    @Inject
    protected CategoryListActivityState categoryListActivityState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((WalletPlus) getApplication()).inject(this);
        mHandler = new Handler();

        mThemedStatusBarColor = getResources().getColor(R.color.theme_primary_dark);
        mNormalStatusBarColor = mThemedStatusBarColor;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
        setupAccountBox();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            Log.w(TAG, "No view with ID main_content to fade in.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Perform one-time bootstrap setup, if needed
        if (!PrefUtils.isDataBootstrapDone(this) && mDataBootstrapThread == null) {
            Log.d(TAG, "One-time data bootstrap not done yet. Doing now.");
            performDataBootstrap();
        }

        startLoginProcess();
    }


    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (isNavDrawerOpen()) {
                closeNavDrawer();
            } else {
                openNavDrawer();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
    }

    protected void openNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    private void startLoginProcess() {
        Long accountId = AccountUtils.getActiveAccountId(this);
        if (accountId == -1) {
            List<Account> accountList = accountService.getAll();
            if (!accountList.isEmpty()) {
                AccountUtils.setActiveAccountId(this, accountList.get(0).getId());
            } else {
                throw new RuntimeException("No accounts available. HANDLE THIS!");
            }
        }
    }

    /**
     * Performs the one-time data bootstrap. This means taking our prepackaged conference data
     * from the R.raw.bootstrap_data resource, and parsing it to populate the database. This
     * data contains the sessions, speakers, etc.
     */
    private void performDataBootstrap() {
        final Context appContext = getApplicationContext();
        mDataBootstrapThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Starting data bootstrap process.");
                // Load data from bootstrap raw resource
                //...
                mDataBootstrapThread = null;
                PrefUtils.markDataBootstrapDone(appContext);
            }
        });
        mDataBootstrapThread.start();
    }

    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.theme_primary_dark));

        final ScrimInsetsScrollView navDrawer = (ScrimInsetsScrollView) mDrawerLayout.findViewById(R.id.navdrawer);
        if (getSelfNavDrawerItem() == DrawerItemType.INVALID) {
            // do not show a nav drawer
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        if (navDrawer != null) {
            final View chosenAccountContentView = findViewById(R.id.chosen_account_content_view);
            final View chosenAccountView = findViewById(R.id.chosen_account_view);
            final int navDrawerChosenAccountHeight = getResources().getDimensionPixelSize(R.dimen.navdrawer_chosen_account_height);
            navDrawer.setOnInsetsCallback(new ScrimInsetsScrollView.OnInsetsCallback() {
                @Override
                public void onInsetsChanged(Rect insets) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) chosenAccountContentView.getLayoutParams();
                    lp.topMargin = insets.top;
                    chosenAccountContentView.setLayoutParams(lp);

                    ViewGroup.LayoutParams lp2 = chosenAccountView.getLayoutParams();
                    lp2.height = navDrawerChosenAccountHeight + insets.top;
                    chosenAccountView.setLayoutParams(lp2);
                }
            });
        }

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(Gravity.START);
                }
            });
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                if (mAccountBoxExpanded) {
                    mAccountBoxExpanded = false;
                    setupAccountBoxToggle();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        // populate the nav drawer with the correct items
        populateNavigationDrawer();

        openDrawerOnWelcome();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        return mActionBarToolbar;
    }

    private void setupNavDrawerViewContent() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[navigationDrawerItemList.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (DrawerItemType type : navigationDrawerItemList) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(type, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final DrawerItemType type, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == type;
        int layoutToInflate;
        if (type == DrawerItemType.SEPARATOR) {
            layoutToInflate = R.layout.navigation_drawer_separator;
        } else {
            layoutToInflate = R.layout.navigation_drawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (type == DrawerItemType.SEPARATOR) {
            // we are done
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = navigationDrawerMap.get(type).icon;
        String title = navigationDrawerMap.get(type).tittle;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(title);

        formatNavDrawerItem(view, type, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(type);
            }
        });

        return view;
    }

    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.INVALID;
    }

    private void formatNavDrawerItem(View view, DrawerItemType drawerItemType, boolean selected) {
        if (drawerItemType == DrawerItemType.SEPARATOR) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private void populateNavigationDrawer() {
        if (navigationDrawerMap.isEmpty()) {
            navigationDrawerMap.put(DrawerItemType.DASHBOARD,
                    new DrawerItemContent(android.R.drawable.ic_media_pause, "Dashboard", DashboardActivity.class));
            navigationDrawerMap.put(DrawerItemType.CASH_FLOW,
                    new DrawerItemContent(android.R.drawable.ic_popup_sync, "Cash flow", CashFlowListActivity.class));
            navigationDrawerMap.put(DrawerItemType.CATEGORY,
                    new DrawerItemContent(android.R.drawable.ic_menu_camera, "Category", CategoryListActivity.class));
            navigationDrawerMap.put(DrawerItemType.WALLET,
                    new DrawerItemContent(android.R.drawable.ic_menu_week, "Wallet", WalletListActivity.class));
        }
        if (navigationDrawerItemList.isEmpty()) {
            navigationDrawerItemList.add(DrawerItemType.DASHBOARD);
            navigationDrawerItemList.add(DrawerItemType.SEPARATOR);
            navigationDrawerItemList.add(DrawerItemType.CASH_FLOW);
            navigationDrawerItemList.add(DrawerItemType.CATEGORY);
            navigationDrawerItemList.add(DrawerItemType.WALLET);
            navigationDrawerItemList.add(DrawerItemType.SEPARATOR);
        }
        setupNavDrawerViewContent();
    }

    private void onNavDrawerItemClicked(final DrawerItemType type) {
        if (type == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }

        if (getSelfNavDrawerItem() == DrawerItemType.CATEGORY) {
            categoryListActivityState.clear();
        }

        if (isSpecialItem(type)) {
            goToNavDrawerItem(type);
        } else {
            // launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(type);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            // change the active type on the list so the user can see the type changed
            setSelectedNavDrawerItem(type);
            // fade out the main content
            View mainContent = findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        mDrawerLayout.closeDrawer(Gravity.START);
    }

    private boolean isSpecialItem(DrawerItemType type) {
        return type == DrawerItemType.SETTINGS;
    }

    private void goToNavDrawerItem(DrawerItemType type) {
        Intent intent = new Intent(this, navigationDrawerMap.get(type).getActivityClass());
        startActivity(intent);
        finish();
    }

    /**
     * Sets up the given navigation_drawer type's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(DrawerItemType drawerItemType) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < navigationDrawerItemList.size()) {
                    DrawerItemType thisType = navigationDrawerItemList.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisType, drawerItemType == thisType);
                }
            }
        }
    }

    protected void onNavDrawerSlide(float offset) {
    }

    private void openDrawerOnWelcome() {
        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!PrefUtils.isWelcomeDone(this)) {
            // first run of the app starts with the nav drawer open
            PrefUtils.markWelcomeDone(this);
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    /**
     * Sets up the account box. The account box is the area at the top of the nav drawer that
     * shows which account the user is logged in as, and lets them switch accounts. It also
     * shows the user's Google+ cover photo as background.
     */
    private void setupAccountBox() {
        mAccountListContainer = (LinearLayout) findViewById(R.id.account_list);

        if (mAccountListContainer == null) {
            //This activity does not have an account box
            return;
        }

        final View chosenAccountView = findViewById(R.id.chosen_account_view);
        final Long chosenAccountId = AccountUtils.getActiveAccountId(this);
        if (chosenAccountId == -1) {
            // No account logged in; hide account box
            chosenAccountView.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            return;
        } else {
            chosenAccountView.setVisibility(View.VISIBLE);
            mAccountListContainer.setVisibility(View.INVISIBLE);
        }

        List<Account> accounts = accountService.getAll();
        Iterables.removeIf(accounts, new Predicate<Account>() {
            @Override
            public boolean apply(Account input) {
                return input.getId().equals(chosenAccountId);
            }
        });

        TextView nameView = (TextView) chosenAccountView.findViewById(R.id.account_name);
        TextView emailView = (TextView) chosenAccountView.findViewById(R.id.account_email);
        mExpandAccountBoxIndicator = (ImageView) findViewById(R.id.expand_account_box_indicator);

        if (accounts.isEmpty()) {
            // There's only one account on the device, so no need for a switcher.
            mExpandAccountBoxIndicator.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            chosenAccountView.setEnabled(false);
            return;
        }

        Account account = accountService.findById(chosenAccountId);
        nameView.setText(account.getName());
        emailView.setText(account.getGmailAccount());

        chosenAccountView.setEnabled(true);
        mExpandAccountBoxIndicator.setVisibility(View.VISIBLE);
        chosenAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccountBoxExpanded = !mAccountBoxExpanded;
                setupAccountBoxToggle();
            }
        });
        setupAccountBoxToggle();

        populateAccountList(accounts);
    }

    private void populateAccountList(List<Account> accounts) {
        mAccountListContainer.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (final Account account : accounts) {
            View itemView = layoutInflater.inflate(R.layout.item_account_list, mAccountListContainer, false);
            ((TextView) itemView.findViewById(R.id.account_name)).setText(account.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AccountUtils.setActiveAccountId(BaseActivity.this, account.getId());
                    mAccountBoxExpanded = false;
                    setupAccountBoxToggle();
                    mDrawerLayout.closeDrawer(Gravity.START);
                    setupAccountBox();
                }
            });
            mAccountListContainer.addView(itemView);
        }
    }

    private void setupAccountBoxToggle() {
        DrawerItemType selfDrawerType = getSelfNavDrawerItem();
        if (mDrawerLayout == null || selfDrawerType == DrawerItemType.INVALID) {
            // this Activity does not have a nav drawer
            return;
        }
        mExpandAccountBoxIndicator.setImageResource(mAccountBoxExpanded
                ? R.drawable.ic_drawer_accounts_collapse
                : R.drawable.ic_drawer_accounts_expand);
        int hideTranslateY = -mAccountListContainer.getHeight() / 4; // last 25% of animation
        if (mAccountBoxExpanded && mAccountListContainer.getTranslationY() == 0) {
            // initial setup
            mAccountListContainer.setAlpha(0);
            mAccountListContainer.setTranslationY(hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mDrawerItemsListContainer.setVisibility(mAccountBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);
                mAccountListContainer.setVisibility(mAccountBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
            }
        });

        if (mAccountBoxExpanded) {
            mAccountListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    ObjectAnimator.ofFloat(mDrawerItemsListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    subSet);
            set.start();
        } else {
            mDrawerItemsListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y,
                            hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    subSet,
                    ObjectAnimator.ofFloat(mDrawerItemsListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.start();
        }

        set.start();
    }

    public enum DrawerItemType {
        DASHBOARD,
        CASH_FLOW,
        CATEGORY,
        WALLET,
        INVALID,
        SETTINGS, SEPARATOR
    }

    private class DrawerItemContent {
        private String tittle;
        private int icon;
        private Class<? extends BaseActivity> activityClass;

        private DrawerItemContent(int icon, String tittle, Class<? extends BaseActivity> activityClass) {
            this.tittle = tittle;
            this.icon = icon;
            this.activityClass = activityClass;
        }

        public String getTittle() {
            return tittle;
        }

        public int getIcon() {
            return icon;
        }

        public Class<? extends BaseActivity> getActivityClass() {
            return activityClass;
        }
    }
}
