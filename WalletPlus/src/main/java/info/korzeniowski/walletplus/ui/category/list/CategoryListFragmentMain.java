package info.korzeniowski.walletplus.ui.category.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import info.korzeniowski.walletplus.KorzeniowskiUtils;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;

public class CategoryListFragmentMain extends Fragment {
    public static final String TAG = "CategoryListFragmentMain";
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

    private CategoryListParcelableState categoryListState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        categoryListState = initOrRestoreState(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private CategoryListParcelableState initOrRestoreState(Bundle savedInstanceState) {
        CategoryListParcelableState restoredState = tryRestore(savedInstanceState);
        return restoredState != null
                ? restoredState
                : initState();
    }

    private CategoryListParcelableState tryRestore(Bundle savedInstanceState) {
        return savedInstanceState != null
                ? (CategoryListParcelableState) savedInstanceState.getParcelable(CATEGORY_LIST_STATE)
                : null;
    }

    private CategoryListParcelableState initState() {
        return new CategoryListParcelableState(Period.WEEK, getMainCategories());
    }

    private boolean isAnyCashflowWithoutCategoryExists() {
        return !localCashFlowService.findCashFlow(null, null, Category.Type.NO_CATEGORY, null, null).isEmpty();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_main_layout, container, false);
        ButterKnife.inject(this, view);
        updateCategoryListState(categoryListState);
        setupViews();
        return view;
    }

    private void updateCategoryListState(CategoryListParcelableState state) {
        state.setCategoryList(getMainCategories());
    }

    private List<Category> getMainCategories() {
        List<Category> mainCategories = localCategoryService.getMainCategories();

        if (isAnyCashflowWithoutCategoryExists()) {
            mainCategories.add(new Category()
                    .setType(Category.Type.NO_CATEGORY)
                    .setName(getString(R.string.categoryNoCategoryName))
                    .setId(UUID.randomUUID().getMostSignificantBits()));
        }

        return mainCategories;
    }

    public void setupViews() {
        CategoryListPagerAdapter pagerAdapter = new CategoryListPagerAdapter();
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(pagerAdapter);

        tabs.setTextColor(getResources().getColor(R.color.white));
        tabs.setBackgroundColor(getResources().getColor(R.color.mainColor));
        tabs.setTabIndicatorColor(getResources().getColor(R.color.lightMainColor));
        pager.setCurrentItem(pager.getAdapter().getCount() / 2);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            startCategoryDetailsFragment();
            return true;
        }
        return false;
    }

    private void startCategoryDetailsFragment() {
        ((MainActivity) getActivity()).setContentFragment(new CategoryDetailsFragment(), true, CategoryDetailsFragment.TAG);
    }

    private class CategoryListPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        int offsetOfCentralPosition;
        private int selectedPage;

        CategoryListPagerAdapter() {
            super(getChildFragmentManager());
            offsetOfCentralPosition = 0;
        }

        @Override
        public int getCount() {
            return 15;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return getFragmentByIteration(getIterationFromPosition(position));
        }

        private Fragment getFragmentByIteration(int iteration) {
            Bundle args = new Bundle();
            args.putParcelable(CATEGORY_LIST_STATE, categoryListState);
            args.putInt(CategoryListFragment2.ITERATION, iteration);
            CategoryListFragment2 fragment = new CategoryListFragment2();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Date fromDate = categoryListState.getStartDate();
            org.joda.time.Period period = getPeriodInJoda(categoryListState.getPeriod());
            int iteration = getIterationFromPosition(position);

            Interval interval = KorzeniowskiUtils.Time.getInterval(new DateTime(fromDate), period, iteration);
            return getPageTitle(interval);
        }

        private int getIterationFromPosition(int position) {
            return offsetOfCentralPosition + position - getCount() / 2;
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

    class CategoryStatList {
        private Node current;
        private Node left;
        private Node right;

        CategoryStatList(List<CategoryService.CategoryStats> stats) {
            current = new Node(stats, 0);
            left = current;
            right = current;
        }

        void addLeft(List<CategoryService.CategoryStats> stats) {
            left.setOnLeft(new Node(stats, current.getIndex() - 1));
            left = left.getLeft();
        }

        void addRight(List<CategoryService.CategoryStats> stats) {
            right.setOnRight(new Node(stats, current.getIndex() + 1));
            right = right.getRight();
        }

        void moveRight() {
            current = current.getRight();
        }

        void moveLeft() {
            current = current.getLeft();
        }

        List<CategoryService.CategoryStats> getCurrent() {
            return current.getStats();
        }

        private class Node {
            private List<CategoryService.CategoryStats> stats;
            private int index;
            private Node left;
            private Node right;

            Node(List<CategoryService.CategoryStats> stats, int index) {
                Node.this.stats = stats;
                Node.this.index = index;
                left = null;
                right = null;
            }

            void setOnLeft(Node node) {
                this.left = node;
                node.right = this;
            }

            void setOnRight(Node node) {
                this.right = node;
                node.left = this;
            }

            Node getLeft() {
                return left;
            }

            Node getRight() {
                return right;
            }

            public List<CategoryService.CategoryStats> getStats() {
                return stats;
            }

            int getIndex() {
                return index;
            }
        }
    }

    public enum Period {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
}
