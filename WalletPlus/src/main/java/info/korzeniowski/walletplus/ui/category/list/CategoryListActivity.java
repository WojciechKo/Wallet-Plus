package info.korzeniowski.walletplus.ui.category.list;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsActivity;
import info.korzeniowski.walletplus.util.KorzeniowskiUtils;

public class CategoryListActivity extends BaseActivity {
    public static final String TAG = CategoryListActivity.class.getSimpleName();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }
        setContentView(R.layout.activity_category_list);
        ButterKnife.inject(this);
        setupViews();

        if (categoryListActivityState.getStartDate() == null) {
            categoryListActivityState.setStartDate(DateTime
                    .now()
                    .withField(DateTimeFieldType.hourOfDay(), 0)
                    .withField(DateTimeFieldType.minuteOfDay(), 0)
                    .toDate());
        }
        if (categoryListActivityState.getPeriod() == null) {
            categoryListActivityState.setPeriod(Period.WEEK);
        }
        categoryListActivityState.setCategoryList(getMainCategories());

        overridePendingTransition(0, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void setupViews() {
        CategoryListPagerAdapter pagerAdapter = new CategoryListPagerAdapter();
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(pagerAdapter);

        tabs.setTextColor(getResources().getColor(R.color.white));
        tabs.setBackgroundColor(getResources().getColor(R.color.theme_primary));
        tabs.setTabIndicatorColor(getResources().getColor(R.color.theme_primary_light));
        pager.setCurrentItem(pager.getAdapter().getCount() / 2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActionBarToolbar().setElevation(0);
        }
    }

    private List<Category> getMainCategories() {
        List<Category> mainCategories = localCategoryService.getMainCategories();

        if (isAnyCashFlowWithoutCategoryExists()) {
            mainCategories.add(new Category()
                    .setId(CategoryService.CATEGORY_NULL_ID)
                    .setName(getString(R.string.categoryNoCategoryName)));
        }

        return mainCategories;
    }

    private boolean isAnyCashFlowWithoutCategoryExists() {
        return !localCashFlowService.findCashFlow(null, null, CategoryService.CATEGORY_NULL_ID, null, null).isEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            Intent intent = new Intent(this, CategoryDetailsActivity.class);
            startActivityForResult(intent, CategoryDetailsActivity.REQUEST_CODE_ADD_CATEGORY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CategoryDetailsActivity.REQUEST_CODE_ADD_CATEGORY) {
            if (resultCode == RESULT_OK) {
                categoryListActivityState.setCategoryList(getMainCategories());
            }
        } else if (requestCode == CategoryDetailsActivity.REQUEST_CODE_EDIT_CATEGORY) {
            if (resultCode == CategoryDetailsActivity.RESULT_DELETED) {
                final Long categoryId = data.getExtras().getLong(CategoryDetailsActivity.RESULT_DATA_DELETED_CATEGORY_ID);
                Iterables.removeIf(categoryListActivityState.getCategoryList(), new Predicate<Category>() {
                    @Override
                    public boolean apply(Category input) {
                        return input.getId().equals(categoryId);
                    }
                });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            return CategoryListFragment.newInstance(getIterationFromPosition(position));
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
            Date fromDate = categoryListActivityState.getStartDate();
            org.joda.time.Period period = getPeriodInJoda(categoryListActivityState.getPeriod());
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
