package info.korzeniowski.walletplus.ui.category.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import org.joda.time.Period;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

public class CategoryListFragment2 extends Fragment {
    public static final String ITERATION = "iteration";

    @InjectView(R.id.list)
    ExpandableListView list;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private CategoryListParcelableState categoryListState;
    private int iteration;
    private List<CategoryService.CategoryStats> categoryStatsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        categoryListState = getArguments().getParcelable(CategoryListFragmentMain.CATEGORY_LIST_STATE);
        iteration = getArguments().getInt(ITERATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.category_list, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    private void setupViews() {
        initFields();
        setupAdapters();
    }

    protected void initFields() {
        Period period = getPeriod(categoryListState.getPeriod());
        categoryStatsList = localCategoryService.getCategoryStateList(categoryListState.getStartDate(), period, iteration);
    }

    private Period getPeriod(CategoryListFragmentMain.Period period) {
        switch (period) {
            case DAY:
                return Period.days(1);
            case WEEK:
                return Period.weeks(1);
            case MONTH:
                return Period.months(1);
            case YEAR:
                return Period.years(1);
        }
        return null;
    }

    private void setupAdapters() {
        list.setAdapter(getCategoryListAdapter());
        removeListListeners();
    }

    private CategoryStatsExpandableListAdapter getCategoryListAdapter() {
        return new CategoryStatsExpandableListAdapter(getActivity(), categoryListState.getCategoryList(), categoryStatsList, new OnContentClickListener() {
            @Override
            public void onContentClick(Long id) {
                startCategoryDetailsFragment(id);
            }
        });
    }


    private void startCategoryDetailsFragment(Long id) {
        Fragment fragment = new CategoryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true, CategoryDetailsFragment.TAG);
    }

    private void removeListListeners() {
        list.setOnGroupClickListener(
                new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                        return true;
                    }
                }
        );
        list.setOnChildClickListener(
                new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        return true;
                    }
                }
        );
    }
}
