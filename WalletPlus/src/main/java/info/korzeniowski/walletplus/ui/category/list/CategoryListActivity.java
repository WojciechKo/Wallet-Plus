package info.korzeniowski.walletplus.ui.category.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsActivity;
import info.korzeniowski.walletplus.util.KorzeniowskiUtils;

public class CategoryListActivity extends BaseActivity {
    public static final String TAG = CategoryListActivity.class.getSimpleName();
    public static final String CATEGORY_LIST_STATE = "cashFlowDetailsState";

    @InjectView(R.id.tabs)
    PagerTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    private CategoryListActivityState activityState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }
        setContentView(R.layout.activity_categories);
        ButterKnife.inject(this);
        setupViews();

        ((WalletPlus) getApplication()).inject(this);
        activityState = initOrRestoreState(savedInstanceState);

        overridePendingTransition(0, 0);
    }

    void setupViews() {
        CategoryListPagerAdapter pagerAdapter = new CategoryListPagerAdapter();
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(pagerAdapter);

        tabs.setTextColor(getResources().getColor(R.color.white));
        tabs.setBackgroundColor(getResources().getColor(R.color.theme_primary));
        tabs.setTabIndicatorColor(getResources().getColor(R.color.theme_primary_light));
        pager.setCurrentItem(pager.getAdapter().getCount() / 2);
    }

    private CategoryListActivityState initOrRestoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return (CategoryListActivityState) savedInstanceState.getParcelable(CATEGORY_LIST_STATE);
        } else {
            return initState();
        }
    }

    private CategoryListActivityState initState() {
        CategoryListActivityState state = new CategoryListActivityState();
        state.setPeriod(Period.WEEK);
        state.setCategoryList(getMainCategories());
        return state;
    }

    private List<Category> getMainCategories() {
        List<Category> mainCategories = localCategoryService.getMainCategories();

        if (isAnyCashFlowWithoutCategoryExists()) {
            mainCategories.add(new Category()
                    .setType(Category.Type.NO_CATEGORY)
                    .setName(getString(R.string.categoryNoCategoryName))
                    .setId(UUID.randomUUID().getMostSignificantBits()));
        }

        return mainCategories;
    }

    private boolean isAnyCashFlowWithoutCategoryExists() {
        return !localCashFlowService.findCashFlow(null, null, Category.Type.NO_CATEGORY, null, null).isEmpty();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CATEGORY_LIST_STATE, activityState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            startCategoryDetailsActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCategoryDetailsActivity() {
        Intent intent = new Intent(this, CategoryDetailsActivity.class);
        startActivity(intent);
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.CATEGORY;
    }

    public enum Period {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    private class CategoryListPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        int offsetOfCentralPosition;
        private int selectedPage;

        CategoryListPagerAdapter() {
            super(getSupportFragmentManager());
            offsetOfCentralPosition = 0;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return CategoryListFragment.newInstance(getIterationFromPosition(position), activityState);
        }

        private int getIterationFromPosition(int position) {
            return offsetOfCentralPosition + position - getCount() / 2;
        }

        @Override
        public int getCount() {
            return 15;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Date fromDate = activityState.getStartDate();
            org.joda.time.Period period = getPeriodInJoda(activityState.getPeriod());
            int iteration = getIterationFromPosition(position);

            Interval interval = KorzeniowskiUtils.Times.getInterval(new DateTime(fromDate), period, iteration);
            return getPageTitle(interval);
        }

        private CharSequence getPageTitle(Interval interval) {
            DateFormat dateFormat = new SimpleDateFormat("dd. MMM");
            return dateFormat.format(interval.getStart().toDate()) + " - " + dateFormat.format(interval.getEnd().toDate());
        }

        private org.joda.time.Period getPeriodInJoda(Period selectedPeriod) {
            switch (selectedPeriod) {
                case DAY:
                    return org.joda.time.Period.days(1);
                case WEEK:
                    return org.joda.time.Period.weeks(1);
                case MONTH:
                    return org.joda.time.Period.months(1);
                case YEAR:
                    return org.joda.time.Period.years(1);
            }
            return null;
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int position) {
            selectedPage = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                handleInfiniteViewPager();
            }
        }

        private void handleInfiniteViewPager() {
            offsetOfCentralPosition += selectedPage - getCount() / 2;
            if (selectedPage != getCount() / 2) {
                pager.setCurrentItem(getCount() / 2, false);
                notifyDataSetChanged();
            }
        }
    }
}
