package com.walletudo.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.walletudo.util.StateManager;
import com.walletudo.util.UIUtils;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class NavigationDrawerHelper extends Fragment {
    private static final String TAG = NavigationDrawerHelper.class.getSimpleName();

    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;
    private static final int NAV_DRAWER_LAUNCH_DELAY = 250;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final Map<DrawerItemType, DrawerItemContent> navigationDrawerMap;

    static {
        navigationDrawerMap = getDrawerMap();
    }

    @Optional
    @InjectView(R.id.navdrawer)
    LinearLayout navDrawer;

    @Optional
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Optional
    @InjectView(R.id.navdrawer_list)
    ViewGroup mNavDrawerListContainer;

    @Optional
    @InjectView(R.id.navdrawer_list_footer)
    LinearLayout mNavDrawerListFooter;

    @Inject
    List<DrawerItemType> navigationDrawerList;

    @Optional
    @InjectView(R.id.expand_account_box_indicator)
    ImageView mExpandAccountBoxIndicator;

    @Inject
    ProfileService profileService;

    private View[] mNavDrawerItemViews = null;

    private boolean profileBoxExpanded = false;

    private LinearLayout mAccountListContainer;

    private LinearLayout mAccountListFooter;

    private Handler mHandler;

    private static Map<DrawerItemType, DrawerItemContent> getDrawerMap() {
        Map<DrawerItemType, DrawerItemContent> navigationDrawerContent = Maps.newHashMap();

        navigationDrawerContent.put(DrawerItemType.DASHBOARD,
                new DrawerItemContent(R.drawable.ic_menu_dashboard, Walletudo.getInstance().getString(R.string.dashboardMenu), DashboardActivity.class));
        navigationDrawerContent.put(DrawerItemType.CASH_FLOW,
                new DrawerItemContent(R.drawable.ic_menu_cash_flow, Walletudo.getInstance().getString(R.string.cashFlowMenu), CashFlowListActivity.class));
        navigationDrawerContent.put(DrawerItemType.STATISTIC,
                new DrawerItemContent(R.drawable.ic_menu_statistic, Walletudo.getInstance().getString(R.string.statisticsMenu), StatisticActivity.class));
        navigationDrawerContent.put(DrawerItemType.WALLET,
                new DrawerItemContent(R.drawable.ic_menu_wallet, Walletudo.getInstance().getString(R.string.walletMenu), WalletListActivity.class));
        navigationDrawerContent.put(DrawerItemType.TAG,
                new DrawerItemContent(R.drawable.ic_menu_tag, Walletudo.getInstance().getString(R.string.tagMenu), TagListActivity.class));
        navigationDrawerContent.put(DrawerItemType.SYNCHRONIZE,
                new DrawerItemContent(R.drawable.ic_menu_synchronization, Walletudo.getInstance().getString(R.string.synchronizationLabel), SynchronizeActivity.class));

        return navigationDrawerContent;
    }

    public static <ParentActivity extends AppCompatActivity> NavigationDrawerHelper attach(ParentActivity parent) {
        return attach(parent.getSupportFragmentManager());
    }

    private static NavigationDrawerHelper attach(FragmentManager fragmentManager) {
        NavigationDrawerHelper frag = (NavigationDrawerHelper) fragmentManager.findFragmentByTag(TAG);
        if (frag == null) {
            frag = new NavigationDrawerHelper();
            fragmentManager.beginTransaction().add(frag, TAG).commit();
        }
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.inject(this, getActivity());
        Walletudo.getInstance().component().inject(this);
        mHandler = new Handler();
        setupNavDrawer();
    }

    private BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public NavigationDrawerHelper setupNavDrawer() {
        if (!isDrawerPresent()) {
            return this;
        }
        drawerLayout.setStatusBarBackgroundColor(getActivity().getResources().getColor(R.color.theme_primary_dark));
        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (profileBoxExpanded) {
                    profileBoxExpanded = false;
                    setupProfileBoxToggle();
                }
            }
        });

        if (getBaseActivity().getSelfNavDrawerItem() == DrawerItemType.INVALID) {
            hideNavDrawer();
            return this;
        }

        ScrimInsetsScrollView navDrawerContent = (ScrimInsetsScrollView) getActivity().findViewById(R.id.navdrawer_content);
        if (navDrawerContent != null) {
            setOnInsetsCallback(navDrawerContent);
        }

        // populate the nav drawer with the correct items
        makeNavDrawerItemsViews();
        makeSettingsMenuInNavDrawerFooter();
        setupProfileBox();
        return this;
    }

    public boolean isDrawerPresent() {
        return drawerLayout != null;
    }

    private void setupProfileBox() {
        mAccountListContainer = (LinearLayout) getActivity().findViewById(R.id.account_list);
        mAccountListFooter = (LinearLayout) getActivity().findViewById(R.id.account_list_footer);

        if (mAccountListContainer == null) {
            //This activity does not have an account box
            return;
        }

        final View profileBox = getActivity().findViewById(R.id.profile_box);
        Profile activeProfile = profileService.getActiveProfile();
        //TODO: remove if
        if (activeProfile == null) {
            // No account logged in; hide account box
            profileBox.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            return;
        } else {
            profileBox.setVisibility(View.VISIBLE);
            mAccountListContainer.setVisibility(View.INVISIBLE);
        }

        TextView nameView = (TextView) profileBox.findViewById(R.id.profile_name);
        nameView.setText(activeProfile.getName());
        profileBox.setEnabled(true);

        profileBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileBoxExpanded = !profileBoxExpanded;
                setupProfileBoxToggle();
            }
        });
        setupProfileBoxToggle();

        List<Profile> profiles = profileService.getAll();
        populateProfileList(profiles);

        setupAccountListFooter();
    }

    private void setupProfileBoxToggle() {
        NavigationDrawerHelper.DrawerItemType selfDrawerType = getBaseActivity().getSelfNavDrawerItem();
        if (drawerLayout == null || selfDrawerType == NavigationDrawerHelper.DrawerItemType.INVALID) {
            // this Activity does not have a nav drawer
            return;
        }
        mExpandAccountBoxIndicator.setImageResource(profileBoxExpanded
                ? R.drawable.ic_drawer_accounts_collapse
                : R.drawable.ic_drawer_accounts_expand);
        int hideTranslateY = -mAccountListContainer.getHeight() / 4; // last 25% of animation
        if (profileBoxExpanded && mAccountListContainer.getTranslationY() == 0) {
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
                mNavDrawerListContainer.setVisibility(profileBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);
                mNavDrawerListFooter.setVisibility(profileBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);

                mAccountListContainer.setVisibility(profileBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
                mAccountListFooter.setVisibility(profileBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
            }
        });

        if (profileBoxExpanded) {
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

    private void makeNavDrawerItemsViews() {
        if (mNavDrawerListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[navigationDrawerList.size()];
        mNavDrawerListContainer.removeAllViews();
        int i = 0;
        for (DrawerItemType type : navigationDrawerList) {
            mNavDrawerItemViews[i] = makeNavDrawerItemView(type, mNavDrawerListContainer);
            mNavDrawerListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private void makeSettingsMenuInNavDrawerFooter() {
        mNavDrawerListFooter.removeAllViews();
        mNavDrawerListFooter.addView(makeNavDrawerItemView(DrawerItemType.SEPARATOR, mNavDrawerListFooter));
        View footerItem = getActivity().getLayoutInflater().inflate(R.layout.item_navigation_drawer, mNavDrawerListFooter, false);
        TextView title = (TextView) footerItem.findViewById(R.id.title);
        ImageView icon = (ImageView) footerItem.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.ic_menu_settings);
        title.setText(getActivity().getString(R.string.settingsMenu));
        footerItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                getActivity().startActivity(intent);
            }
        });
        mNavDrawerListFooter.addView(footerItem);
    }

    private void setOnInsetsCallback(ScrimInsetsScrollView navDrawerContent) {
        final View chosenAccountContentView = getActivity().findViewById(R.id.chosen_account_content_view);
        final View chosenAccountView = getActivity().findViewById(R.id.profile_box);
        final int navDrawerChosenAccountHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.navdrawer_chosen_account_height);
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

    private void hideNavDrawer() {
        // do not show a nav drawer
        if (navDrawer != null) {
            ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
        }
        drawerLayout = null;
    }

    public boolean isNavDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeNavDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void openNavDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void onNavDrawerItemClicked(final DrawerItemType type) {
        if (type == ((BaseActivity) getActivity()).getSelfNavDrawerItem()) {
            closeNavDrawer();
            return;
        }

        StateManager.clearStates(getActivity());

        if (isSpecialItem(type)) {
            goToNavDrawerItem(type);
        } else {
            // launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(type);
                }
            }, NAV_DRAWER_LAUNCH_DELAY);

            // change the active type on the list so the user can see the type changed
            setSelectedNavDrawerItem(type);
            // fade out the main content
            View mainContent = getActivity().findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }


    private boolean isSpecialItem(DrawerItemType type) {
        return type == DrawerItemType.SETTINGS;
    }


    private void goToNavDrawerItem(DrawerItemType type) {
        Intent intent = new Intent(getActivity(), navigationDrawerMap.get(type).getActivityClass());
        getActivity().startActivity(intent);
        getActivity().finish();
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
                getActivity().getResources().getColor(R.color.navdrawer_text_color_selected) :
                getActivity().getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getActivity().getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getActivity().getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private void setupAccountListFooter() {
        mAccountListFooter.removeAllViews();

        mAccountListFooter.addView(makeNavDrawerItemView(DrawerItemType.SEPARATOR, mAccountListFooter));

        View newProfileView = getActivity().getLayoutInflater().inflate(R.layout.item_navigation_drawer, mAccountListFooter, false);
        TextView title = (TextView) newProfileView.findViewById(R.id.title);
        title.setText(getString(R.string.addProfileMenu));

        newProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                getActivity().startActivityForResult(intent, RequestCode.NEW_PROFILE_RC);
            }
        });
        mAccountListFooter.addView(newProfileView);
    }


    private void populateProfileList(List<Profile> profiles) {
        mAccountListContainer.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        for (final Profile profile : profiles) {
            View itemView = layoutInflater.inflate(R.layout.item_navigation_drawer, mAccountListContainer, false);
            TextView profileNameView = (TextView) itemView.findViewById(R.id.title);
            profileNameView.setText(profile.getName());
            ProfileService profileService = ((Walletudo) getActivity().getApplication()).component().profileService();

            if (profile.getId().equals(profileService.getActiveProfile().getId())) {
                profileNameView.setTextColor(getResources().getColor(R.color.navdrawer_text_color_selected));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        profileBoxExpanded = false;
                        setupProfileBoxToggle();
                        closeNavDrawer();
                    }
                });
            } else {
                profileNameView.setTextColor(getResources().getColor(R.color.navdrawer_text_color));

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
        PrefUtils prefUtils = ((Walletudo) getActivity().getApplication()).component().prefUtils();
        prefUtils.setActiveProfileId(id);
        AndroidUtils.restartApplication(getActivity());
    }

    private View makeNavDrawerItemView(final NavigationDrawerHelper.DrawerItemType type, ViewGroup container) {
        boolean selected = getBaseActivity().getSelfNavDrawerItem() == type;
        int layoutToInflate;
        if (type == NavigationDrawerHelper.DrawerItemType.SEPARATOR) {
            layoutToInflate = R.layout.item_navigation_drawer_separator;
        } else {
            layoutToInflate = R.layout.item_navigation_drawer;
        }
        View view = getActivity().getLayoutInflater().inflate(layoutToInflate, container, false);

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
}
