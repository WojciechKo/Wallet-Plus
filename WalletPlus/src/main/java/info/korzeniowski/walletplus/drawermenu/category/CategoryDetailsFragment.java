package info.korzeniowski.walletplus.drawermenu.category;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.service.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.widget.ListenWhenDisabledToggleButton;

@OptionsMenu(R.menu.action_save)
@EFragment(R.layout.category_details)
public class CategoryDetailsFragment extends Fragment {
    private enum DetailsType {ADD, EDIT}

    static final public String CATEGORY_ID = "CATEGORY_ID";

    @ViewById
    TextView categoryNameLabel;

    @ViewById
    EditText categoryName;

    @ViewById
    RadioGroup categoryType;

    @ViewById
    ListenWhenDisabledToggleButton categoryIncomeType;

    @ViewById
    ListenWhenDisabledToggleButton categoryExpenseType;

    @ViewById
    Spinner parentCategory;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private DetailsType mType;
    private Category mCategory;
    private Category mParent;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Long categoryId = getArguments().getLong(CATEGORY_ID);
        mType = categoryId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        mCategory = getCategory(categoryId);
        mParent = mCategory.getParent();
        return null;
    }

    private Category getCategory(Long categoryId) {
        if (mType.equals(DetailsType.EDIT)) {
            return localCategoryService.findById(categoryId);
        }
        return new Category().setType(Category.Type.INCOME_EXPENSE);
    }

    @AfterViews
    void setupViews() {
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    private void setupAdapters() {
        parentCategory.setAdapter(new ParentCategoryAdapter(getActivity(), localCategoryService.getMainCategories()));
    }

    private void setupListeners() {
        ListenWhenDisabledToggleButton.OnClickWhenDisabledListener typeButtonClickedListener = new ListenWhenDisabledToggleButton.OnClickWhenDisabledListener() {
            @Override
            public void onClickWhenDisable() {
                Toast.makeText(getActivity(), "Can't change type of subcategory.", Toast.LENGTH_SHORT).show();
            }
        };
        categoryIncomeType.setOnClickWhenDisabledListener(typeButtonClickedListener);
        categoryExpenseType.setOnClickWhenDisabledListener(typeButtonClickedListener);

        parentCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    noParentSelected();
                } else {
                    parentSelected((Category) parentView.getSelectedItem());
                }
            }

            private void noParentSelected() {
                if (mParent != null) {
                    categoryIncomeType.setChecked(mParent.isIncomeType());
                    categoryExpenseType.setChecked(mParent.isExpenseType());
                    mParent = null;
                }
                categoryIncomeType.setEnabled(true);
                categoryExpenseType.setEnabled(true);
            }

            private void parentSelected(Category selectedParent) {
                mParent = selectedParent;
                categoryIncomeType.setChecked(selectedParent.isIncomeType());
                categoryExpenseType.setChecked(selectedParent.isExpenseType());
                categoryIncomeType.setEnabled(false);
                categoryExpenseType.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private void fillViewsWithData() {
        categoryName.setText(mCategory.getName());
        if (mCategory.getParent() != null) {
            ParentCategoryAdapter parentCategoryAdapter = (ParentCategoryAdapter) parentCategory.getAdapter();
            parentCategory.setSelection(parentCategoryAdapter.getPosition(mParent));
        }
        categoryIncomeType.setChecked(mCategory.isIncomeType());
        categoryExpenseType.setChecked(mCategory.isExpenseType());
    }

    private void getDataFromViews() {
        mCategory.setName(categoryName.getText().toString());
        if (mParent == null) {
            mCategory.setParent(null);
            mCategory.setType(getTypeFromView());
        } else {
            mCategory.setParent(mParent);
            mCategory.setType(null);
        }
    }

    private Category.Type getTypeFromView() {
        if (categoryExpenseType.isChecked() && categoryIncomeType.isChecked()) {
            return Category.Type.INCOME_EXPENSE;
        } else if (categoryIncomeType.isChecked()) {
            return Category.Type.INCOME;
        } else if (categoryExpenseType.isChecked()) {
            return Category.Type.EXPENSE;
        }
        return null;
    }

    @OptionsItem(R.id.menu_save)
    void actionSave() {
        if (preValidation()) {
            getDataFromViews();
            boolean success = false;
            if (DetailsType.ADD.equals(mType)) {
                success = tryInsert();
            } else if (DetailsType.EDIT.equals(mType)) {
                success = tryUpdate();
            }
            if (success) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    private boolean preValidation() {
        return validateName() && validateType();
    }

    private boolean validateName() {
        if (Strings.isNullOrEmpty(categoryName.getText().toString())) {
            categoryName.setError("Category must have name.");
            return false;
        } else if (isContainLeadingOrTrailingSpaces(categoryName.getText().toString())) {
            categoryName.setError("Category name can't contain leading or trailing spaces.");
            return false;
        }
        return true;
    }

    private boolean isContainLeadingOrTrailingSpaces(String string) {
        return !string.trim().equals(string);
    }

    private boolean validateType() {
        if (mParent == null && !isTypeChosen()) {
            showToast("Main category must have any type.");
            return false;
        }
        return true;
    }

    private boolean isTypeChosen() {
        return categoryIncomeType.isChecked() || categoryExpenseType.isChecked();
    }

    private boolean tryInsert() {
        try {
            localCategoryService.insert(mCategory);
            return true;
        } catch (CategoryNameMustBeUniqueException e) {
            categoryName.setError("Category name need to be unique");
        }
        return false;
    }

    private boolean tryUpdate() {
        try {
            if (mCategory.getParent() != null) {
                mCategory.setType(null);
            }
            localCategoryService.update(mCategory);
            return true;
        } catch (CategoryNameMustBeUniqueException e) {
            categoryName.setError("Category name need to be unique");
        } catch (CategoryHaveSubsException e) {
            showToast("Subcategory cannot have subcategories.");
        }
        return false;
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private class ParentCategoryAdapter extends BaseAdapter {
        private Context context;
        private List<Category> mainCategories;

        private ParentCategoryAdapter(Context context, List<Category> mainCategories) {
            mainCategories.add(0, new Category().setName("No parent (main category)"));
            this.context = context;
            this.mainCategories = mainCategories;
        }

        @Override
        public int getCount() {
            return mainCategories.size();
        }

        @Override
        public Category getItem(int position) {
            return mainCategories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public int getPosition(final Category category) {
            return Iterables.indexOf(mainCategories, new Predicate<Category>() {
                @Override
                public boolean apply(Category categoryIt) {
                    return category.getId().equals(categoryIt.getId());
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
            }
            TextView categoryNameView = (TextView) convertView.findViewById(android.R.id.text1);

            categoryNameView.setText(getItem(position).getName());

            return convertView;
        }

    }
}
