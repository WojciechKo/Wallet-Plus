package info.korzeniowski.walletplus.ui.category.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.astuetz.PagerSlidingTabStrip;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.KorzeniowskiUtils;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.service.CategoryService;

public class CategoryListFragmentMain extends Fragment {
    public static final String TAG = "CategoryListFragmentMain";
    public static final String CATEGORY_LIST_STATE = "cashFlowDetailsState";

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private Spinner spinner;
    private CategoryListPagerAdapter pagerAdapter;
    private CategoryListParcelableState categoryListState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        categoryListState = initOrRestoreState(savedInstanceState);
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
        CategoryListParcelableState state = new CategoryListParcelableState(Period.WEEK);
        return state;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_layout, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    public void setupViews() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.category_type_spinner_item, CategoryType.valuesString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) getActivity().findViewById(R.id.toolbarSubtitle);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedType = CategoryType.values()[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setVisibility(View.VISIBLE);

        pagerAdapter = new CategoryListPagerAdapter();
        pager.setAdapter(pagerAdapter);

        tabs.setViewPager(pager);
        tabs.setTextColorResource(R.color.white);
        tabs.setBackgroundColor(getResources().getColor(R.color.mainColor));
        tabs.setIndicatorColorResource(R.color.darkerMainColor);
        tabs.setUnderlineColorResource(android.R.color.transparent);
        tabs.setShouldExpand(true);

        pager.setCurrentItem(pager.getAdapter().getCount()/2);
    }

    @Override
    public void onStop() {
        spinner.setVisibility(View.INVISIBLE);
        spinner = null;
        super.onStop();
    }

    private class CategoryListPagerAdapter extends FragmentPagerAdapter {

        CategoryListPagerAdapter() {
            super(getFragmentManager());
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Fragment getItem(int i) {
            Bundle args = new Bundle();
            args.putParcelable(CATEGORY_LIST_STATE, categoryListState);
            args.putInt(CategoryListFragment2.ITERATION, i - getCount() / 2);
            CategoryListFragment2 fragment = new CategoryListFragment2();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Date fromDate = categoryListState.getStartDate();
            org.joda.time.Period period = getPeriodInJoda(categoryListState.getPeriod());
            int iteration = position - getCount() / 2;

            Interval interval = KorzeniowskiUtils.Time.getInterval(new DateTime(fromDate), period, iteration);
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

    public enum CategoryType {
        INCOME,
        EXPANSE,
        BOTH;

        public static List<String> valuesString() {
            List<String> result = Lists.newArrayListWithCapacity(CategoryType.values().length);
            for (CategoryType type : CategoryType.values()) {
                result.add(type.name().toLowerCase());
            }
            return result;
        }
    }

    public enum Period {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
}
