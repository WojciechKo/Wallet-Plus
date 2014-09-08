package info.korzeniowski.walletplus.ui.category.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;

public class CategoryListsContainerFragment extends Fragment {
    public final static String TAG = "CategoryListsContainerFragment";

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tab_layout, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    public void setupViews() {
        pager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        tabs.setViewPager(pager);
        tabs.setTextColorResource(R.color.black);
        tabs.setIndicatorColorResource(R.color.actionBarBackground);
        tabs.setUnderlineColorResource(android.R.color.transparent);
        tabs.setShouldExpand(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == info.korzeniowski.walletplus.R.id.menu_new) {
            startCategoryDetailsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCategoryDetailsFragment() {
        Fragment fragment = new CategoryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, 0L);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true, CategoryDetailsFragment.TAG);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private LinkedList<Map.Entry<String, Integer>> list;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            list = new LinkedList<Map.Entry<String, Integer>>();
            list.add(new AbstractMap.SimpleEntry<String, Integer>("income", CategoryListFragment.ONLY_INCOME));
            list.add(new AbstractMap.SimpleEntry<String, Integer>("expense", CategoryListFragment.ONLY_EXPENSE));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return list.get(position).getKey();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Fragment getItem(int position) {
            CategoryListFragment fragment = new CategoryListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(CategoryListFragment.CATEGORY_TYPE, list.get(position).getValue());
            fragment.setArguments(bundle);
            return fragment;
        }
    }
}