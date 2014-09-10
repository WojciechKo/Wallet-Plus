package info.korzeniowski.walletplus.ui.cashflow.details;

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
import com.google.common.collect.Lists;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowBaseDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowExpanseDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowIncomeDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowTransferDetailsFragment;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;

public class CashFlowDetailsContainerFragment extends Fragment {
    public static final String TAG = "cashFlowDetailsContainer";
    public static final String CASH_FLOW_ID = "CASH_FLOW_ID";

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
        Long cashFlowId = getArguments().getLong(CashFlowDetailsContainerFragment.CASH_FLOW_ID);
        pager.setAdapter(new CashFlowPagerAdapter(getChildFragmentManager(), cashFlowId));
        tabs.setViewPager(pager);
        tabs.setTextColorResource(R.color.black);
        tabs.setIndicatorColorResource(R.color.actionBarBackground);
        tabs.setUnderlineColorResource(android.R.color.transparent);
        tabs.setShouldExpand(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_new, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == info.korzeniowski.walletplus.R.id.menu_new) {
            actionAdd();
            return true;
        }
        return false;
    }

    void actionAdd() {
        startCashFlowDetailsFragment();
    }

    private void startCashFlowDetailsFragment() {
        Fragment fragment = new CategoryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, 0L);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true, CategoryDetailsFragment.TAG);
    }

    public class CashFlowPagerAdapter extends FragmentPagerAdapter {
        private List<CashFlowBaseDetailsFragment> fragmentList;
        private String[] titles = {"income", "transfer", "expanse"};
        private CashFlowDetailsParcelableState state;

        public CashFlowPagerAdapter(FragmentManager fm, long id) {
            this(fm);
            state.setId(id);
        }

        public CashFlowPagerAdapter(FragmentManager fm) {
            super(fm);
            state = new CashFlowDetailsParcelableState();
            fragmentList = Lists.newArrayList();
            Bundle bundle = new Bundle();
            bundle.putParcelable(CashFlowBaseDetailsFragment.CASH_FLOW_DETAILS_STATE, state);
            CashFlowBaseDetailsFragment fragment = new CashFlowIncomeDetailsFragment();
            fragment.setArguments(bundle);
            fragmentList.add(fragment);
            fragment = new CashFlowTransferDetailsFragment();
            fragment.setArguments(bundle);
            fragmentList.add(fragment);
            fragment = new CashFlowExpanseDetailsFragment();
            fragment.setArguments(bundle);
            fragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
    }
}
