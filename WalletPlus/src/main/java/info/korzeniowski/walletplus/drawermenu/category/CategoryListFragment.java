package info.korzeniowski.walletplus.drawermenu.category;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.model.Category;

/**
 * Fragment with list of categories.
 */
@OptionsMenu(R.menu.action_new)
@EFragment(R.layout.category_list)
public class CategoryListFragment extends Fragment {
    public static final String CATEGORY_TYPE = "categoryType";
    public static final int ONLY_INCOME = 1;
    public static final int ONLY_EXPENSE = ONLY_INCOME + 1;
    public static final int ALL = ONLY_EXPENSE + 1;

    @ViewById(R.id.superList)
    ExpandableListView superList;

    @Inject @Named("local")
    CategoryDataManager localCategoryDataManager;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupViews() {
        setHasOptionsMenu(true);
        int type = getArguments().getInt(CATEGORY_TYPE);
        superList.setAdapter(new CategoryListAdapter(getActivity(), getCategoryList(type)));
        superList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = ((ExpandableListView) parent).getExpandableListPosition(position);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

                ExpandableListAdapter expandableListAdapter = ((ExpandableListView) parent).getExpandableListAdapter();
                long categoryId;
                if (childPosition == -1)
                    categoryId = expandableListAdapter.getGroupId(groupPosition);
                else
                    categoryId = expandableListAdapter.getChildId(groupPosition, childPosition);

                ((ActionBarActivity) getActivity()).startSupportActionMode(new CategoryLongPressCallback(categoryId));
                return true;
            }
        });
    }

    @OptionsItem(R.id.menu_new)
    void actionAdd() {
        startCategoryDetailsFragment();
    }

    private void startCategoryDetailsFragment() {
        startCategoryDetailsFragment(0L);
    }

    private void startCategoryDetailsFragment(Long id) {
        Fragment fragment= new CategoryDetailsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(CategoryDetailsFragment.CATEGORY_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true);
    }

    private List<Category> getCategoryList(int type) {
        switch (type) {
            case ONLY_INCOME:
                return localCategoryDataManager.getMainIncomeTypeCategories();
            case ONLY_EXPENSE:
                return localCategoryDataManager.getMainExpenseTypeCategories();
            case ALL:
                return localCategoryDataManager.getMainCategories();
        }
        throw new RuntimeException("Inacceptable category type: " + type);
    }

    private final class CategoryLongPressCallback implements ActionMode.Callback {
        private final Long id;

        public CategoryLongPressCallback(Long id) {
            this.id = id;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, android.view.Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.action_edit_delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, android.view.Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
            switch(menuItem.getItemId()) {
                case R.id.menu_edit:
                    startCategoryDetailsFragment(id);
                    break;
                case R.id.menu_delete:
                    try {
                        localCategoryDataManager.deleteById(id);
                    } catch (CategoryHaveSubsException e) {
                        buildIfDeleteWithSubcategoryAlertDialog().show();
                    }
                    break;
            }
            actionMode.finish();
            return true;
        }

        private AlertDialog buildIfDeleteWithSubcategoryAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(localCategoryDataManager.findById(id).getName());
            builder.setMessage(R.string.category_have_children + "\n\n" + R.string.do_you_want_to_delete_with_subcategories);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    localCategoryDataManager.deleteByIdWithSubcategories(id);
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            return builder.create();
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }
    }
}