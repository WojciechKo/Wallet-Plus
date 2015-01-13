package info.korzeniowski.walletplus.ui.category.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.joda.time.Period;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsActivity;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

public class CategoryListFragment extends Fragment {
    public static final String ITERATION = "iteration";

    @InjectView(R.id.list)
    ExpandableListView list;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Inject
    CategoryListActivityState categoryListActivityState;

    private int iteration;

    public static CategoryListFragment newInstance(int iteration) {
        CategoryListFragment fragment = new CategoryListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ITERATION, iteration);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        iteration = getArguments().getInt(ITERATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.category_list, container, false);
        ButterKnife.inject(this, view);
        removeListListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        list.setAdapter(getCategoryListAdapter());
    }

    private CategoryStatsExpandableListAdapter getCategoryListAdapter() {
        return new CategoryStatsExpandableListAdapter(
                getActivity(),
                categoryListActivityState.getCategoryList(),
                getCategoryStatsList(),
                new OnContentClickListener<Category>() {
                    @Override
                    public void onContentClick(Category category) {
                        if (category.getType() == Category.Type.NO_CATEGORY) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.categoryNoCategoryClicked), Toast.LENGTH_SHORT).show();
                        } else {
                            startCategoryDetailsActivity(category.getId());
                        }
                    }
                },
                null);
    }

    private List<CategoryService.CategoryStats> getCategoryStatsList() {
        return localCategoryService.getCategoryStatsList(categoryListActivityState.getStartDate(), getPeriod(categoryListActivityState.getPeriod()), iteration);
    }

    private Period getPeriod(CategoryListActivity.Period period) {
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

    private void startCategoryDetailsActivity(Long id) {
        Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
        intent.putExtra(CategoryDetailsActivity.EXTRAS_CATEGORY_ID, id);
        startActivity(intent);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
