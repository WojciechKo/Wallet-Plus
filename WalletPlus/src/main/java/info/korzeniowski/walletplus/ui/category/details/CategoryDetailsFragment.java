package info.korzeniowski.walletplus.ui.category.details;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;

public class CategoryDetailsFragment extends Fragment {
    public static final String TAG = CategoryDetailsActivity.class.getSimpleName();

    public static final String ARGUMENT_CATEGORY_ID = "CATEGORY_ID";

    @InjectView(R.id.categoryNameLabel)
    TextView categoryNameLabel;

    @InjectView(R.id.categoryName)
    EditText categoryName;

    @InjectView(R.id.parentCategory)
    Spinner parentCategoryView;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private DetailsAction detailsAction;
    private Optional<Category> categoryToEdit;

    public static CategoryDetailsFragment newInstance(Long categoryId) {
        CategoryDetailsFragment fragment = new CategoryDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(ARGUMENT_CATEGORY_ID, categoryId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ((WalletPlus) getActivity().getApplication()).inject(this);

        Long categoryId = getArguments() == null ? -1 : getArguments().getLong(ARGUMENT_CATEGORY_ID);

        if (categoryId == -1) {
            detailsAction = DetailsAction.ADD;
            categoryToEdit = Optional.absent();
        } else {
            detailsAction = DetailsAction.EDIT;
            categoryToEdit = Optional.of(localCategoryService.findById(categoryId));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_category_details, container, false);
        ButterKnife.inject(this, view);

        List<Category> mainCategories = localCategoryService.getMainCategories();
        mainCategories.add(0, new Category().setId(CategoryService.CATEGORY_NULL_ID).setName(getString(R.string.categoryNoParentSelected)));
        parentCategoryView.setAdapter(new ParentCategoryAdapter(getActivity(), mainCategories));

        if (savedInstanceState == null && detailsAction == DetailsAction.EDIT) {
            mainCategories.remove(categoryToEdit.get());
            categoryName.setText(categoryToEdit.get().getName());
            if (categoryToEdit.get().getParent() != null) {
                parentCategoryView.setSelection(mainCategories.indexOf(categoryToEdit.get().getParent()));
            }
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_save) {
            onSaveOptionSelected();
            return true;
        }
        return result;
    }

    void onSaveOptionSelected() {
        if (Strings.isNullOrEmpty(categoryName.getText().toString())) {
            categoryName.setError(getString(R.string.categoryMustHaveName));
            return;
        }

        if (categoryName.getError() == null) {
            Category categoryToSave = new Category();
            categoryToSave.setParent(getSelectedParentFromView());
            categoryToSave.setName(categoryName.getText().toString());

            if (detailsAction == DetailsAction.ADD) {
                localCategoryService.insert(categoryToSave);
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

            } else if (detailsAction == DetailsAction.EDIT) {
                try {
                    categoryToSave.setId(categoryToEdit.get().getId());
                    localCategoryService.update(categoryToSave);
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } catch (CategoryHaveSubsException e) {
                    Toast.makeText(getActivity(), R.string.categorySubCantHaveSubs, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Category getSelectedParentFromView() {
        Category selectedParent = (Category) parentCategoryView.getSelectedItem();
        if (CategoryService.CATEGORY_NULL_ID.equals(selectedParent.getId())) {
            return null;
        }
        return selectedParent;
    }

    private enum DetailsAction {ADD, EDIT}

    static class ParentCategoryAdapter extends ArrayAdapter<Category> {
        private final WeakReference<Context> context;

        ParentCategoryAdapter(Context context, List<Category> mainCategories) {
            super(context, 0);
            this.context = new WeakReference<>(context);
            addAll(mainCategories);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ParentCategoryViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context.get()).inflate(android.R.layout.simple_spinner_item, null);
                holder = new ParentCategoryViewHolder();
                ButterKnife.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ParentCategoryViewHolder) convertView.getTag();
            }

            Category item = getItem(position);
            holder.categoryName.setText(item.getName());

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        class ParentCategoryViewHolder {
            @InjectView(android.R.id.text1)
            TextView categoryName;
        }
    }
}
