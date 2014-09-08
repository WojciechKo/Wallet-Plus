package info.korzeniowski.walletplus.ui.category.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.ui.category.details.CategoryDetailsFragment;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

/**
 * Fragment with list of categories.
 */
public class CategoryListFragment extends Fragment {
    public static final String TAG = "categoryList";
    public static final String CATEGORY_TYPE = "categoryType";
    public static final int ONLY_INCOME = 1;
    public static final int ONLY_EXPENSE = ONLY_INCOME + 1;
    public static final int ALL = ONLY_EXPENSE + 1;

    @InjectView(R.id.list)
    ExpandableListView list;

    @Inject @Named("local")
    CategoryService localCategoryService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.category_list, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    void setupViews() {
        setListAdapter(getArguments().getInt(CATEGORY_TYPE));
        removeListListeners();
    }

    private void setListAdapter(int categoryType) {
        list.setAdapter(
                new CategoryExpandableListAdapter(getActivity(), getCategoryList(categoryType),
                        new OnContentClickListener() {
                            @Override
                            public void onContentClick(Long id) {
                                startCategoryDetailsFragment(id);
                            }
                        }
                )
        );
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

    private void startCategoryDetailsFragment(Long id) {
        Fragment fragment = new CategoryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true, CategoryDetailsFragment.TAG);
    }

    private List<Category> getCategoryList(int type) {
        switch (type) {
            case ONLY_INCOME:
                return localCategoryService.getMainIncomeTypeCategories();
            case ONLY_EXPENSE:
                return localCategoryService.getMainExpenseTypeCategories();
            case ALL:
                return localCategoryService.getMainCategories();
        }
        throw new RuntimeException("Inacceptable category type: " + type);
    }
}