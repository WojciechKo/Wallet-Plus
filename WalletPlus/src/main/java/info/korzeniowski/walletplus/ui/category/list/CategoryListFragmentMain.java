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
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
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
    private CategoryType selectedType;
    private Period selectedPeriod = Period.DAY;
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
        CategoryListParcelableState state = new CategoryListParcelableState();
        return state;
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
                selectedType = CategoryType.values()[position];
                showCategoryStatsGroupByPeriod(selectedType, selectedPeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setVisibility(View.VISIBLE);

        pagerAdapter = new CategoryListPagerAdapter();
        pager.setAdapter(pagerAdapter);

        pager.setOffscreenPageLimit(3);
        tabs.setViewPager(pager);
        tabs.setTextColorResource(R.color.white);
        tabs.setBackgroundColor(getResources().getColor(R.color.mainColor));
        tabs.setIndicatorColorResource(R.color.darkerMainColor);
        tabs.setUnderlineColorResource(android.R.color.transparent);
        tabs.setShouldExpand(true);
    }

    private void showCategoryStatsGroupByPeriod(CategoryType selectedType, Period selectedPeriod) {
        org.joda.time.Period period = getPeriodInJoda(selectedPeriod);
        Toast.makeText(getActivity(), "Odpalam fragment:" + selectedType + ":" + period, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        spinner.setVisibility(View.INVISIBLE);
        spinner = null;
        super.onStop();
    }

    private class CategoryListPagerAdapter extends FragmentPagerAdapter{

        private CategoryStatList categoryStatList;

        CategoryListPagerAdapter() {
            super(getFragmentManager());
            List<CategoryService.CategoryStats> stat = localCategoryService.getCategoryStateList(categoryListState.getFromDate(), getPeriodInJoda(categoryListState.getPeriod()),0);
            categoryStatList = new CategoryStatList(stat);
            categoryStatList.addLeft(localCategoryService.getCategoryStateList(categoryListState.getFromDate(), getPeriodInJoda(categoryListState.getPeriod()),-1));
            categoryStatList.addRight(localCategoryService.getCategoryStateList(categoryListState.getFromDate(), getPeriodInJoda(categoryListState.getPeriod()),1));
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Fragment getItem(int i) {
            Integer half = getCount()/2;
            Bundle args = new Bundle();
            args.putParcelable(CATEGORY_LIST_STATE, categoryListState);
            args.putInt(CategoryListFragment2.ITERATION, i - half);
            CategoryListFragment2 fragment = new CategoryListFragment2();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (categoryListState.getPeriod()) {
                case DAY:
                    return "12. March - 13.March"
            }
            return "Iter:" + position;
        }

        //
//        @Override
//        public void onPageSelected(int position) {
//            selectedPage = position;
//        }
//
//        @Override
//        public void onPageScrolled(int arg0, float arg1, int arg2) {
//        }
//
//        @Override
//        public void onPageScrollStateChanged(int state) {
//            if (state == ViewPager.SCROLL_STATE_IDLE) {
//
//                final PageModel leftPage = mPageModel[PAGE_LEFT];
//                final PageModel middlePage = mPageModel[PAGE_MIDDLE];
//                final PageModel rightPage = mPageModel[PAGE_RIGHT];
//
//                final int oldLeftIndex = leftPage.getIndex();
//                final int oldMiddleIndex = middlePage.getIndex();
//                final int oldRightIndex = rightPage.getIndex();
//
//                // user swiped to right direction --> left page
//                if (mSelectedPageIndex == PAGE_LEFT) {
//
//                    // moving each page content one page to the right
//                    leftPage.setIndex(oldLeftIndex - 1);
//                    middlePage.setIndex(oldLeftIndex);
//                    rightPage.setIndex(oldMiddleIndex);
//
//                    setContent(PAGE_RIGHT);
//                    setContent(PAGE_MIDDLE);
//                    setContent(PAGE_LEFT);
//
//                    // user swiped to left direction --> right page
//                } else if (mSelectedPageIndex == PAGE_RIGHT) {
//
//                    leftPage.setIndex(oldMiddleIndex);
//                    middlePage.setIndex(oldRightIndex);
//                    rightPage.setIndex(oldRightIndex + 1);
//
//                    setContent(PAGE_LEFT);
//                    setContent(PAGE_MIDDLE);
//                    setContent(PAGE_RIGHT);
//                }
//                viewPager.setCurrentItem(PAGE_MIDDLE, false);
//            }
//        }

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
