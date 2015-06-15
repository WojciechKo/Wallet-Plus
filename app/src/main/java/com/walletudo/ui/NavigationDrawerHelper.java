package com.walletudo.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.samples.apps.iosched.ui.widget.ScrimInsetsScrollView;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.model.Profile;
import com.walletudo.service.ProfileService;
import com.walletudo.ui.cashflow.list.CashFlowListActivity;
import com.walletudo.ui.dashboard.DashboardActivity;
import com.walletudo.ui.profile.ProfileActivity;
import com.walletudo.ui.settings.SettingsActivity;
import com.walletudo.ui.statistics.StatisticActivity;
import com.walletudo.ui.synchronize.SynchronizeActivity;
import com.walletudo.ui.tag.list.TagListActivity;
import com.walletudo.ui.wallets.list.WalletListActivity;
import com.walletudo.util.AndroidUtils;
import com.walletudo.util.PrefUtils;
import com.walletudo.util.RequestCode;
import com.walletudo.util.UIUtils;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NavigationDrawerHelper {
    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;

    private BaseActivity activity;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private final Map<DrawerItemType, DrawerItemContent> navigationDrawerMap;

    @Inject
    List<DrawerItemType> navigationDrawerList;

    // views that correspond to each navigation_drawer type, null if not yet created
    private View[] mNavDrawerItemViews = null;
    private boolean mAccountBoxExpanded = false;
    private LinearLayout mNavDrawerListFooter;
    private ImageView mExpandAccountBoxIndicator;
    private Handler mHandler;

    private LinearLayout mAccountListContainer;
    private LinearLayout mAccountListFooter;
    private ViewGroup mNavDrawerListContainer;

    public NavigationDrawerHelper(BaseActivity activity) {
        this.activity = activity;
        ButterKnife.inject(this, activity);
        navigationDrawerMap = getDrawerMap();
        mHandler = new Handler();
        ((Walletudo) activity.getApplication()).component().inject(this);
    }

    private Map<DrawerItemType, DrawerItemContent> getDrawerMap() {
        Map<DrawerItemType, DrawerItemContent> navigationDrawerContent = Maps.newHashMap();

        navigationDrawerContent.put(DrawerItemType.DASHBOARD,
                new DrawerItemContent(R.drawable.ic_menu_dashboard, activity.getString(R.string.dashboardMenu), DashboardActivity.class));
        navigationDrawerContent.put(DrawerItemType.CASH_FLOW,
                new DrawerItemContent(R.drawable.ic_menu_cash_flow, activity.getString(R.string.cashFlowMenu), CashFlowListActivity.class));
        navigationDrawerContent.put(DrawerItemType.STATISTIC,
                new DrawerItemContent(R.drawable.ic_menu_statistic, activity.getString(R.string.statisticsMenu), StatisticActivity.class));
        navigationDrawerContent.put(DrawerItemType.WALLET,
                new DrawerItemContent(R.drawable.ic_menu_wallet, activity.getString(R.string.walletMenu), WalletListActivity.class));
        navigationDrawerContent.put(DrawerItemType.TAG,
                new DrawerItemContent(R.drawable.ic_menu_tag, activity.getString(R.string.tagMenu), TagListActivity.class));
        navigationDrawerContent.put(DrawerItemType.SYNCHRONIZE,
                new DrawerItemContent(R.drawable.ic_menu_synchronization, activity.getString(R.string.synchronizationLabel), SynchronizeActivity.class));

        return navigationDrawerContent;
    }
    public boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void openNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public NavigationDrawerHelper setupNavDrawer() {
        Preconditions.checkNotNull(activity);

        if (mDrawerLayout == null) {
            return this;
        }
        mDrawerLayout.setStatusBarBackgroundColor(activity.getResources().getColor(R.color.theme_primary_dark));

        final LinearLayout navDrawer = (LinearLayout) mDrawerLayout.findViewById(R.id.navdrawer);

        if (activity.getSelfNavDrawerItem() == DrawerItemType.INVALID) {
            // do not show a nav drawer
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return this;
        }

        final ScrimInsetsScrollView navDrawerContent = (ScrimInsetsScrollView) navDrawer.findViewById(R.id.navdrawer_content);

        if (navDrawerContent != null) {
            final View chosenAccountContentView = activity.findViewById(R.id.chosen_account_content_view);
            final View chosenAccountView = activity.findViewById(R.id.chosen_account_view);
            final int navDrawerChosenAccountHeight = activity.getResources().getDimensionPixelSize(R.dimen.navdrawer_chosen_account_height);
            navDrawerContent.setOnInsetsCallback(new ScrimInsetsScrollView.OnInsetsCallback() {
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

        Toolbar mActionBarToolbar = (Toolbar) activity.findViewById(R.id.toolbar_actionbar);
        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mAccountBoxExpanded) {
                    mAccountBoxExpanded = false;
                    setupAccountBoxToggle();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        // populate the nav drawer with the correct items
        setupNavDrawerViewContent();
        setupNavDrawerFooter();
        setupAccountBox();
        return this;
    }

    private void setupNavDrawerViewContent() {
        mNavDrawerListContainer = (ViewGroup) activity.findViewById(R.id.navdrawer_list);

        if (mNavDrawerListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[navigationDrawerList.size()];
        mNavDrawerListContainer.removeAllViews();
        int i = 0;
        for (DrawerItemType type : navigationDrawerList) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(type, mNavDrawerListContainer);
            mNavDrawerListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private void setupNavDrawerFooter() {
        mNavDrawerListFooter = (LinearLayout) activity.findViewById(R.id.navdrawer_list_footer);
        mNavDrawerListFooter.removeAllViews();
        mNavDrawerListFooter.addView(makeNavDrawerItem(DrawerItemType.SEPARATOR, mNavDrawerListFooter));
        View footerItem = activity.getLayoutInflater().inflate(R.layout.item_navigation_drawer, mNavDrawerListFooter, false);
        TextView title = (TextView) footerItem.findViewById(R.id.title);
        ImageView icon = (ImageView) footerItem.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.ic_menu_settings);
        title.setText(activity.getString(R.string.settingsMenu));
        footerItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, SettingsActivity.class);
                activity.startActivity(intent);
            }
        });
        mNavDrawerListFooter.addView(footerItem);
    }


    private void onNavDrawerItemClicked(final DrawerItemType type) {
        if (type == activity.getSelfNavDrawerItem()) {
            closeNavDrawer();
            return;
        }

        //TODO: cos z tym zrobic!
//        if (activity.getSelfNavDrawerItem() == BaseActivity.DrawerItemType.STATISTIC) {
//            statisticListActivityState.clear();
//        }

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
            View mainContent = activity.findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private boolean isSpecialItem(DrawerItemType type) {
        return type == DrawerItemType.SETTINGS;
    }

    private void goToNavDrawerItem(DrawerItemType type) {
        Intent intent = new Intent(activity, navigationDrawerMap.get(type).getActivityClass());
        activity.startActivity(intent);
        activity.finish();
    }


    /**
     * Sets up the given navigation_drawer type's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(NavigationDrawerHelper.DrawerItemType drawerItemType) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < navigationDrawerList.size()) {
                    DrawerItemType thisType = navigationDrawerList.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisType, drawerItemType == thisType);
                }
            }
        }
    }


    private void formatNavDrawerItem(View view, NavigationDrawerHelper.DrawerItemType drawerItemType, boolean selected) {
        if (drawerItemType == NavigationDrawerHelper.DrawerItemType.SEPARATOR) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                activity.getResources().getColor(R.color.navdrawer_text_color_selected) :
                activity.getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                activity.getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                activity.getResources().getColor(R.color.navdrawer_icon_tint));
    }


    private void setupAccountBoxToggle() {
        NavigationDrawerHelper.DrawerItemType selfDrawerType = activity.getSelfNavDrawerItem();
        if (mDrawerLayout == null || selfDrawerType == NavigationDrawerHelper.DrawerItemType.INVALID) {
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

            mAccountListFooter.setAlpha(0);
            mAccountListFooter.setTranslationY(-hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mNavDrawerListContainer.setVisibility(mAccountBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);
                mNavDrawerListFooter.setVisibility(mAccountBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);

                mAccountListContainer.setVisibility(mAccountBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
                mAccountListFooter.setVisibility(mAccountBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
            }
        });

        if (mAccountBoxExpanded) {
            mAccountListContainer.setVisibility(View.VISIBLE);
            mAccountListFooter.setVisibility(View.VISIBLE);

            AnimatorSet hideNavDrawerList = new AnimatorSet();
            hideNavDrawerList.playTogether(
                    ObjectAnimator.ofFloat(mNavDrawerListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mNavDrawerListFooter, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION)
            );

            AnimatorSet showAccountList = new AnimatorSet();
            showAccountList.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),

                    ObjectAnimator.ofFloat(mAccountListFooter, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListFooter, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));

            set.playSequentially(
                    hideNavDrawerList,
                    showAccountList);
        } else {
            mNavDrawerListContainer.setVisibility(View.VISIBLE);
            mNavDrawerListFooter.setVisibility(View.VISIBLE);

            AnimatorSet hideAccountList = new AnimatorSet();
            hideAccountList.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y, hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),

                    ObjectAnimator.ofFloat(mAccountListFooter, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListFooter, View.TRANSLATION_Y, -hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));

            AnimatorSet showNavDrawerList = new AnimatorSet();
            showNavDrawerList.playTogether(
                    ObjectAnimator.ofFloat(mNavDrawerListFooter, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mNavDrawerListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));

            set.playSequentially(
                    hideAccountList,
                    showNavDrawerList);
        }

        set.start();
    }


    /**
     * Sets up the account box. The account box is the area at the top of the nav drawer that
     * shows which account the user is logged in as, and lets them switch accounts. It also
     * shows the user's Google+ cover photo as background.
     */
    private void setupAccountBox() {
        mAccountListContainer = (LinearLayout) activity.findViewById(R.id.account_list);
        mAccountListFooter = (LinearLayout) activity.findViewById(R.id.account_list_footer);

        if (mAccountListContainer == null) {
            //This activity does not have an account box
            return;
        }

        final View chosenAccountView = activity.findViewById(R.id.chosen_account_view);
        ProfileService profileService = ((Walletudo) activity.getApplication()).component().profileService();
        Profile activeProfile = profileService.getActiveProfile();
        if (activeProfile == null) {
            // No account logged in; hide account box
            chosenAccountView.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            return;
        } else {
            chosenAccountView.setVisibility(View.VISIBLE);
            mAccountListContainer.setVisibility(View.INVISIBLE);
        }

        List<Profile> profiles = profileService.getAll();

        TextView nameView = (TextView) chosenAccountView.findViewById(R.id.profile_name);
        TextView emailView = (TextView) chosenAccountView.findViewById(R.id.account_email);
        mExpandAccountBoxIndicator = (ImageView) activity.findViewById(R.id.expand_account_box_indicator);

//      TODO: Remove this.
//        if (profiles.isEmpty()) {
//            // There's only one account on the device, so no need for a switcher.
//            mExpandAccountBoxIndicator.setVisibility(View.GONE);
//            mAccountListContainer.setVisibility(View.GONE);
//            chosenAccountView.setEnabled(false);
//            return;
//        }

        nameView.setText(activeProfile.getName());
        emailView.setText(activeProfile.getGoogleAccount());

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

        populateProfileList(profiles);

        setupAccountListFooter();
    }

    private void setupAccountListFooter() {
        mAccountListFooter.removeAllViews();

        mAccountListFooter.addView(makeNavDrawerItem(DrawerItemType.SEPARATOR, mAccountListFooter));

        View newProfileView = activity.getLayoutInflater().inflate(R.layout.item_navigation_drawer, mAccountListFooter, false);
        TextView title = (TextView) newProfileView.findViewById(R.id.title);
        title.setText(activity.getString(R.string.addProfileMenu));

        newProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivityForResult(intent, RequestCode.NEW_PROFILE_RC);
            }
        });
        mAccountListFooter.addView(newProfileView);
    }


    private void populateProfileList(List<Profile> profiles) {
        mAccountListContainer.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        for (final Profile profile : profiles) {
            View itemView = layoutInflater.inflate(R.layout.item_navigation_drawer, mAccountListContainer, false);
            TextView profileNameView = (TextView) itemView.findViewById(R.id.title);
            profileNameView.setText(profile.getName());
            ProfileService profileService = ((Walletudo) activity.getApplication()).component().profileService();

            if (profile.getId().equals(profileService.getActiveProfile().getId())) {
                profileNameView.setTextColor(activity.getResources().getColor(R.color.navdrawer_text_color_selected));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAccountBoxExpanded = false;
                        setupAccountBoxToggle();
                        closeNavDrawer();
                    }
                });
            } else {
                profileNameView.setTextColor(activity.getResources().getColor(R.color.navdrawer_text_color));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectProfileById(profile.getId());
                    }
                });
            }
            mAccountListContainer.addView(itemView);
        }
    }


    public void selectProfileById(Long id) {
        PrefUtils prefUtils = ((Walletudo) activity.getApplication()).component().prefUtils();
        prefUtils.setActiveProfileId(id);
        AndroidUtils.restartApplication(activity);
    }

    public enum DrawerItemType {
        DASHBOARD,
        CASH_FLOW,
        STATISTIC,
        WALLET,
        TAG,
        INVALID,
        SETTINGS,
        SEPARATOR,
        SYNCHRONIZE
    }

    public static class DrawerItemContent {
        private String tittle;
        private int icon;
        private Class<? extends BaseActivity> activityClass;

        public DrawerItemContent(int icon, String tittle, Class<? extends BaseActivity> activityClass) {
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

    private View makeNavDrawerItem(final NavigationDrawerHelper.DrawerItemType type, ViewGroup container) {
        boolean selected = activity.getSelfNavDrawerItem() == type;
        int layoutToInflate;
        if (type == NavigationDrawerHelper.DrawerItemType.SEPARATOR) {
            layoutToInflate = R.layout.item_navigation_drawer_separator;
        } else {
            layoutToInflate = R.layout.item_navigation_drawer;
        }
        View view = activity.getLayoutInflater().inflate(layoutToInflate, container, false);

        if (type == NavigationDrawerHelper.DrawerItemType.SEPARATOR) {
            // we are done
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = navigationDrawerMap.get(type).getIcon();
        String title = navigationDrawerMap.get(type).getTittle();

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
}
