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
    Spinner parentCategoryView;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private DetailsType type;
    private List<Category> parentCategoryList;
    private Category category;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        initState();
    }

    private void initState() {
        category = localCategoryService.findById(getArguments().getLong(CATEGORY_ID));
        if (category == null) {
            category = new Category();
            type = DetailsType.ADD;
        } else {
            type = DetailsType.EDIT;
        }
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
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    private void setupAdapters() {
        parentCategoryList = localCategoryService.getMainCategories();
        parentCategoryView.setAdapter(new ParentCategoryAdapter(getActivity(), parentCategoryList));
    }

    private void setupListeners() {
        ListenWhenDisabledToggleButton.OnClickWhenDisabledListener typeButtonClickedListener = new ListenWhenDisabledToggleButton.OnClickWhenDisabledListener() {
            @Override
            public void onClickWhenDisable() {
                showToast(getActivity().getString(R.string.categoryCantChangeTypeOfSubcategory));
            }
        };
        categoryIncomeType.setOnClickWhenDisabledListener(typeButtonClickedListener);
        categoryExpenseType.setOnClickWhenDisabledListener(typeButtonClickedListener);
    }

    private void fillViewsWithData() {
        categoryName.setText(category.getName());
        Category.Type type = category.getType();
        if (category.getParent() != null) {
            Category parentCategory = localCategoryService.findById(category.getParent().getId());
            parentCategoryView.setSelection(parentCategoryList.indexOf(parentCategory));
            type = parentCategory.getType();
        }
        categoryIncomeType.setChecked(type == null || type.isIncome());
        categoryExpenseType.setChecked(type == null || type.isExpense());
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
        if (category.getParent() != null) {
            Category parent = localCategoryService.findById(category.getParent().getId());
            categoryIncomeType.setChecked(parent.isIncomeType());
            categoryExpenseType.setChecked(parent.isExpenseType());
            category.setParent(null);
        }
        categoryIncomeType.setEnabled(true);
        categoryExpenseType.setEnabled(true);
    }

    private void parentSelected(Category selectedParent) {
        category.setParent(selectedParent);
        categoryIncomeType.setChecked(selectedParent.isIncomeType());
        categoryExpenseType.setChecked(selectedParent.isExpenseType());
        categoryIncomeType.setEnabled(false);
        categoryExpenseType.setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_delete, menu);
        inflater.inflate(R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            selectedOptionDelete();
            return true;
        } else if (item.getItemId() == R.id.menu_save) {
            selectedOptionSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void selectedOptionDelete() {
        if (handleDeleteOption()) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean handleDeleteOption() {
        return type != DetailsType.EDIT || tryDelete();
    }

    private boolean tryDelete() {
        try {
            //TODO: show warning alert about number of cashflows to be deleted
            localCategoryService.deleteById(category.getId());
            return true;
        } catch (CategoryHaveSubsException e) {
            showToast(getActivity().getString(R.string.categoryCantDeleteCategoryWithSubs));
        }
        return false;
    }

    void selectedOptionSave() {
        if (preValidation()) {
            getDataFromViews();
            if (handleSaveOption()) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    private boolean preValidation() {
        return validateName() && validateType();
    }

    private boolean validateName() {
        if (Strings.isNullOrEmpty(categoryName.getText().toString())) {
            categoryName.setError(getActivity().getString(R.string.categoryMustHaveName));
            return false;
        }
        return true;
    }

    private boolean validateType() {
        if (category.getParent() == null && !isTypeChosen()) {
            showToast(getActivity().getString(R.string.categoryMustHaveType));
            return false;
        }
        return true;
    }

    private boolean isTypeChosen() {
        return categoryIncomeType.isChecked() || categoryExpenseType.isChecked();
    }

    private void getDataFromViews() {
        category.setName(categoryName.getText().toString().trim());
        if (category.getParent() == null) {
            category.setType(getTypeFromView());
        } else {
            category.setType(null);
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

    private boolean handleSaveOption() {
        if (type == DetailsType.ADD) {
            return tryInsert();
        } else if (type == DetailsType.EDIT) {
            return tryUpdate();
        }
        return false;
    }

    private boolean tryInsert() {
        localCategoryService.insert(category);
        return true;
    }

    private boolean tryUpdate() {
        try {
            localCategoryService.update(category);
            return true;
        } catch (CategoryHaveSubsException e) {
            showToast(getActivity().getString(R.string.categorySubCantHaveSubs));
        }
        return false;
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    class ParentCategoryAdapter extends BaseAdapter {
        private Context context;
        private List<Category> mainCategories;

        ParentCategoryAdapter(Context context, List<Category> mainCategories) {
            mainCategories.add(0, new Category().setName(context.getString(R.string.categoryNoParentSelected)));
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
