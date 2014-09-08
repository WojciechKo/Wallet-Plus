package info.korzeniowski.walletplus.ui.category;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import info.korzeniowski.walletplus.service.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.widget.ListenWhenDisabledToggleButton;

public class CategoryDetailsFragment extends Fragment {
    public static final String TAG = "categoryDetails";
    public static final String CATEGORY_ID = "CATEGORY_ID";

    private enum DetailsType {ADD, EDIT}

    @InjectView(R.id.categoryNameLabel)
    TextView categoryNameLabel;

    @InjectView(R.id.categoryName)
    EditText categoryName;

    @InjectView(R.id.categoryType)
    RadioGroup categoryType;

    @InjectView(R.id.categoryIncomeType)
    ListenWhenDisabledToggleButton categoryIncomeType;

    @InjectView(R.id.categoryExpenseType)
    ListenWhenDisabledToggleButton categoryExpenseType;

    @InjectView(R.id.parentCategory)
    Spinner parentCategory;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private DetailsType type;
    private Category.Builder categoryBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((WalletPlus) getActivity().getApplication()).inject(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.category_details, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    void setupViews() {
        initFields();
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_delete, menu);
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == info.korzeniowski.walletplus.R.id.menu_delete) {
            actionDelete();
            return true;
        }
        if (itemId_ == info.korzeniowski.walletplus.R.id.menu_save) {
            actionSave();
            return true;
        }
        return false;
    }

    private void initFields() {
        Long categoryId = getArguments().getLong(CATEGORY_ID);
        type = categoryId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        categoryBuilder = new Category.Builder(localCategoryService.findById(categoryId));
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
                Category parent = categoryBuilder.getParent();
                if (parent != null) {
                    categoryIncomeType.setChecked(parent.isIncomeType());
                    categoryExpenseType.setChecked(parent.isExpenseType());
                    categoryBuilder.setParent(null);
                }
                categoryIncomeType.setEnabled(true);
                categoryExpenseType.setEnabled(true);
            }

            private void parentSelected(Category selectedParent) {
                categoryBuilder.setParent(selectedParent);
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
        categoryName.setText(categoryBuilder.getName());
        Category.Type type = categoryBuilder.getType();
        if (categoryBuilder.getParent() != null) {
            ParentCategoryAdapter parentCategoryAdapter = (ParentCategoryAdapter) parentCategory.getAdapter();
            parentCategory.setSelection(parentCategoryAdapter.getPosition(categoryBuilder.getParent()));
            type = categoryBuilder.getParent().getType();
        }

        categoryIncomeType.setChecked(type == null || type.isIncome());
        categoryExpenseType.setChecked(type == null || type.isExpense());
    }

    private void getDataFromViews() {
        categoryBuilder.setName(categoryName.getText().toString());
        if (categoryBuilder.getParent() == null) {
            categoryBuilder.setType(getTypeFromView());
        } else {
            categoryBuilder.setType(null);
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

    void actionSave() {
        if (preValidation()) {
            getDataFromViews();
            if (handleActionSave()) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    private boolean handleActionSave() {
        if (type == DetailsType.ADD) {
            return tryInsert();
        } else if (type == DetailsType.EDIT) {
            return tryUpdate();
        }
        return false;
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
        if (categoryBuilder.getParent() == null && !isTypeChosen()) {
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
            localCategoryService.insert(categoryBuilder.build());
            return true;
        } catch (CategoryNameMustBeUniqueException e) {
            categoryName.setError("Category name need to be unique");
        }
        return false;
    }

    private boolean tryUpdate() {
        try {
            if (categoryBuilder.getParent() != null) {
                categoryBuilder.setType(null);
            }
            localCategoryService.update(categoryBuilder.build());
            return true;
        } catch (CategoryNameMustBeUniqueException e) {
            categoryName.setError("Category name need to be unique");
        } catch (CategoryHaveSubsException e) {
            showToast("Subcategory cannot have subcategories.");
        }
        return false;
    }

    void actionDelete() {
        if (handleDeleteAction()) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean handleDeleteAction() {
        if (type == DetailsType.EDIT) {
            return tryDelete();
        }
        return type == DetailsType.ADD;
    }

    private boolean tryDelete() {
        try {
            // show warning alert about number of cashflows to be deleted
            localCategoryService.deleteById(categoryBuilder.getId());
            return true;
        } catch (CategoryHaveSubsException e) {
            showToast("Cannot delete category which have subcategories.");
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
            mainCategories.add(0, new Category.Builder().setName("No parent (main category)").build());
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
