package info.korzeniowski.walletplus.ui.category.details;

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

import com.google.common.base.Strings;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemSelected;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.service.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.widget.IdentityableListAdapter;
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

    @Inject @Named("local")
    CategoryService localCategoryService;

    private DetailsType type;
    private List<Category> parentCategoryList;
    private CategoryDetailsParcelableState categoryDetailsState;

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

    private void initFields() {
        Long categoryId = getArguments().getLong(CATEGORY_ID);
        type = categoryId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        categoryDetailsState = new CategoryDetailsParcelableState(localCategoryService.findById(categoryId));
//        categoryBuilder = new Category.Builder();
    }

    private void setupAdapters() {
        parentCategoryList = localCategoryService.getMainCategories();
        parentCategory.setAdapter(new ParentCategoryAdapter(getActivity(), parentCategoryList));
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
    }

    private void fillViewsWithData() {
        categoryName.setText(categoryDetailsState.getName());
        Category.Type type = categoryDetailsState.getType();
        if (categoryDetailsState.getParentId() != null) {
            ParentCategoryAdapter parentCategoryAdapter = (ParentCategoryAdapter) parentCategory.getAdapter();
            Category parentCategory = localCategoryService.findById(categoryDetailsState.getParentId());
            this.parentCategory.setSelection(parentCategoryList.indexOf(parentCategory));
            type = parentCategory.getType();
        }

        categoryIncomeType.setChecked(type == null || type.isIncome());
        categoryExpenseType.setChecked(type == null || type.isExpense());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_delete, menu);
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == info.korzeniowski.walletplus.R.id.menu_delete) {
            actionDelete();
            return true;
        } else if (item.getItemId() == info.korzeniowski.walletplus.R.id.menu_save) {
            actionSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            localCategoryService.deleteById(categoryDetailsState.getId());
            return true;
        } catch (CategoryHaveSubsException e) {
            showToast("Cannot delete category which have subcategories.");
        }
        return false;
    }

    void actionSave() {
        if (preValidation()) {
            getDataFromViews();
            if (handleActionSave()) {
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
        if (categoryDetailsState.getParentId() == null && !isTypeChosen()) {
            showToast("Main category must have any type.");
            return false;
        }
        return true;
    }

    private boolean isTypeChosen() {
        return categoryIncomeType.isChecked() || categoryExpenseType.isChecked();
    }

    private void getDataFromViews() {
        categoryDetailsState.setName(categoryName.getText().toString());
        if (categoryDetailsState.getParentId() == null) {
            categoryDetailsState.setType(getTypeFromView());
        } else {
            categoryDetailsState.setType(null);
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

    private boolean handleActionSave() {
        if (type == DetailsType.ADD) {
            return tryInsert();
        } else if (type == DetailsType.EDIT) {
            return tryUpdate();
        }
        return false;
    }

    private boolean tryInsert() {
        try {
            localCategoryService.insert(new Category(categoryDetailsState));
            return true;
        } catch (CategoryNameMustBeUniqueException e) {
            categoryName.setError("Category name need to be unique");
        }
        return false;
    }

    private boolean tryUpdate() {
        try {
            localCategoryService.update(new Category(categoryDetailsState));
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

    @OnItemSelected(R.id.parentCategory)
    public void onParentCategorySelected(AdapterView<?> parentView, int position) {
        if (position == 0) {
            noParentSelected();
        } else {
            parentSelected((Category) parentView.getSelectedItem());
        }
    }

    private void noParentSelected() {
        if (categoryDetailsState.getParentId() != null) {
            Category parent = localCategoryService.findById(categoryDetailsState.getParentId());
            categoryIncomeType.setChecked(parent.isIncomeType());
            categoryExpenseType.setChecked(parent.isExpenseType());
            categoryDetailsState.setParentId(null);
        }
        categoryIncomeType.setEnabled(true);
        categoryExpenseType.setEnabled(true);
    }

    private void parentSelected(Category selectedParent) {
        categoryDetailsState.setParentId(selectedParent.getId());
        categoryIncomeType.setChecked(selectedParent.isIncomeType());
        categoryExpenseType.setChecked(selectedParent.isExpenseType());
        categoryIncomeType.setEnabled(false);
        categoryExpenseType.setEnabled(false);
    }

    public static class ParentCategoryAdapter extends BaseAdapter {
        private Context context;
        private List<Category> mainCategories;

        ParentCategoryAdapter(Context context, List<Category> mainCategories) {
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ParentCategoryViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, null);
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

        class ParentCategoryViewHolder {
            @InjectView(android.R.id.text1)
            TextView categoryName;
        }
    }
}
