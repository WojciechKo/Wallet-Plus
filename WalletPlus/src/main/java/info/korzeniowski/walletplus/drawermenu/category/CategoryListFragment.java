package info.korzeniowski.walletplus.drawermenu.category;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ExpandableListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

/**
 * Fragment with list of categories.
 */

@EFragment(R.layout.category_list)
public class CategoryListFragment extends Fragment {
    public static final String CATEGORY_TYPE = "categoryType";
    public static final int ONLY_INCOME = 1;
    public static final int ONLY_EXPENSE = ONLY_INCOME + 1;
    public static final int ALL = ONLY_EXPENSE + 1;

    @ViewById(R.id.superList)
    ExpandableListView superList;

    @Inject @Named("local")
    CategoryService localCategoryService;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupViews() {
        setHasOptionsMenu(true);
        int categoryType = getArguments().getInt(CATEGORY_TYPE);
        superList.setAdapter(new CategoryExpandableListAdapter(getActivity(), getCategoryList(categoryType), new OnContentClickListener() {
            @Override
            public void onContentClick(Long id) {
                startCategoryDetailsFragment(id);
            }
        }));

        superList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        superList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return true;
            }
        });
    }

    private void startCategoryDetailsFragment(Long id) {
        Fragment fragment = new CategoryDetailsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true);
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