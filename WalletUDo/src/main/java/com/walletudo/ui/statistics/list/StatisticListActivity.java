package com.walletudo.ui.statistics.list;

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
import com.walletudo.R;
import com.walletudo.model.Tag;
import com.walletudo.ui.BaseActivity;
import com.walletudo.ui.statistics.details.StaticticDetailsActivity;
import com.walletudo.util.KorzeniowskiUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class StatisticListActivity extends BaseActivity {
    public static final String TAG = StatisticListActivity.class.getSimpleName();

    PagerTabStrip tabs;

    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }
        setContentView(R.layout.activity_drawer);
        ButterKnife.inject(this);
        setupViews();

        if (statisticListActivityState.getStartDate() == null) {
            statisticListActivityState.setStartDate(
                    DateTime.now()
                            .withField(DateTimeFieldType.dayOfWeek(), 1)
                            .withField(DateTimeFieldType.hourOfDay(), 0)
                            .withField(DateTimeFieldType.minuteOfDay(), 0)
                            .toDate());
        }
        if (statisticListActivityState.getPeriod() == null) {
            statisticListActivityState.setPeriod(Period.WEEK);
        }
        statisticListActivityState.setTagList(getMainCategories());

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

    private List<Tag> getMainCategories() {
        return tagService.getAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            Intent intent = new Intent(this, StaticticDetailsActivity.class);
            startActivityForResult(intent, StaticticDetailsActivity.REQUEST_CODE_ADD_CATEGORY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StaticticDetailsActivity.REQUEST_CODE_ADD_CATEGORY) {
            if (resultCode == RESULT_OK) {
                statisticListActivityState.setTagList(getMainCategories());
            }
        } else if (requestCode == StaticticDetailsActivity.REQUEST_CODE_EDIT_CATEGORY) {
            if (resultCode == StaticticDetailsActivity.RESULT_DELETED) {
                final Long categoryId = data.getExtras().getLong(StaticticDetailsActivity.RESULT_DATA_DELETED_TAG_ID);
                Iterables.removeIf(statisticListActivityState.getTagList(), new Predicate<Tag>() {
                    @Override
                    public boolean apply(Tag input) {
                        return input.getId().equals(categoryId);
                    }
                });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.STATISTIC;
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
            return StatisticListFragment.newInstance(getIterationFromPosition(position));
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
            Date fromDate = statisticListActivityState.getStartDate();
            org.joda.time.Period period = getPeriodInJoda(statisticListActivityState.getPeriod());
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
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            selectedPage = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                handleInfiniteScrolling();
            }
        }

        private void handleInfiniteScrolling() {
            int margin = 3;
            if (selectedPage <= margin) {
                centerToPosition(selectedPage);
            } else if (selectedPage >= getCount() - margin - 1) {
                centerToPosition(selectedPage);
            }
        }

        private void centerToPosition(int position) {
            offsetOfCentralPosition += (position - getCount() / 2);
            notifyDataSetChanged();
            pager.setCurrentItem(getCount() / 2, false);
        }
    }
}
