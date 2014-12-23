package info.korzeniowski.walletplus.ui.category.list;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;
import info.korzeniowski.walletplus.widget.OnContentClickListener;
import info.korzeniowski.walletplus.widget.OnContentLongClickListener;

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

    private CategoryListParcelableState categoryListState;
    private int iteration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        categoryListState = getArguments().getParcelable(CategoryListFragmentMain.CATEGORY_LIST_STATE);
        iteration = getArguments().getInt(ITERATION);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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
        setupAdapters();
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
        return new CategoryStatsExpandableListAdapter(
                getActivity(),
                categoryListState.getCategoryList(),
                getCategoryStatsList(),
                new OnContentClickListener<Category>() {
                    @Override
                    public void onContentClick(Category category) {
                        if (category.getType() == Category.Type.NO_CATEGORY) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.categoryNoCategoryClicked), Toast.LENGTH_SHORT).show();
                        } else {
                            startCategoryDetailsFragment(category.getId());
                        }
                    }
                },
                new OnContentLongClickListener<Category>() {
                    @Override
                    public void onContentLongClick(Category category) {
                        if (category.getType() == Category.Type.NO_CATEGORY) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.categoryNoCategoryClicked), Toast.LENGTH_SHORT).show();
                        } else {
                            showDeleteConfirmationAlert(category);
                        }
                    }
                });
    }

    private List<CategoryService.CategoryStats> getCategoryStatsList() {
        return localCategoryService.getCategoryStatsList(categoryListState.getStartDate(), getPeriod(categoryListState.getPeriod()), iteration);
    }

    private void showDeleteConfirmationAlert(final Category category) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getConfirmationMessage(category))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryDeleteCategory(category);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private String getConfirmationMessage(Category category) {
        Integer count = localCashFlowService.findCashFlow(null, null, category.getId(), null, null).size();
        return "Do you want to delete category:\n" + category.getName() + "\n\n" + count + " cashflows will be assigned to no category.";
    }


    private void tryDeleteCategory(Category category) {
        try {
            localCategoryService.deleteById(category.getId());
            categoryListState.getCategoryList().remove(category);
            list.setAdapter(getCategoryListAdapter());
        } catch (CategoryHaveSubsException e) {
            //TODO: alert if delete with subs
            Toast.makeText(getActivity(), R.string.categoryCantDeleteCategoryWithSubs, Toast.LENGTH_SHORT).show();
        }
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
