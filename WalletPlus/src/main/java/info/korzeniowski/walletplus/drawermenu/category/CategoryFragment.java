package info.korzeniowski.walletplus.drawermenu.category;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;

@OptionsMenu(R.menu.action_new)
@EFragment(R.layout.tab_layout)
public class CategoryFragment extends Fragment {

    @ViewById
    PagerSlidingTabStrip tabs;

    @ViewById
    ViewPager pager;

    @AfterViews
    public void setupViews() {
        pager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        tabs.setViewPager(pager);
        tabs.setTextColorResource(R.color.black);
        tabs.setIndicatorColorResource(R.color.actionBarBackground);
        tabs.setUnderlineColorResource(android.R.color.transparent);
        tabs.setShouldExpand(true);
    }

    @OptionsItem(R.id.menu_new)
    void actionAdd() {
        startCategoryDetailsFragment();
    }

    private void startCategoryDetailsFragment() {
        Fragment fragment = new CategoryDetailsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, 0L);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = { "INCOME", "EXPENSE"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            CategoryListFragment fragment = new CategoryListFragment_();
            Bundle bundle = new Bundle();
            bundle.putInt(CategoryListFragment.CATEGORY_TYPE, getCategoryType(position));
            fragment.setArguments(bundle);
            return fragment;
        }

        private int getCategoryType(int position) {
            if (position == 0) {
                return CategoryListFragment.ONLY_INCOME;
            } else if(position == 1) {
                return CategoryListFragment.ONLY_EXPENSE;
            }
            throw new RuntimeException("OutOfBound.");
        }
    }
}