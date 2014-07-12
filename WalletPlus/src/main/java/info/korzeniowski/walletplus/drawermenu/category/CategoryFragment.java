package info.korzeniowski.walletplus.drawermenu.category;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.korzeniowski.walletplus.R;

public class CategoryFragment extends Fragment {
    FragmentTabHost tabHost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_host_fragment, container, false);
        tabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        tabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        Bundle incomeBundle = new Bundle();
        incomeBundle.putInt(CategoryListFragment.CATEGORY_TYPE, CategoryListFragment.ONLY_INCOME);
        tabHost.addTab(tabHost.newTabSpec("income").setIndicator("Income"), CategoryListFragment_.class, incomeBundle);

        Bundle expenseBundle = new Bundle();
        expenseBundle.putInt(CategoryListFragment.CATEGORY_TYPE, CategoryListFragment.ONLY_EXPENSE);
        tabHost.addTab(tabHost.newTabSpec("expence").setIndicator("Expence"), CategoryListFragment_.class, expenseBundle);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tabHost = null;
    }
}