package info.korzeniowski.walletplus.drawermenu.category;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.model.Category;

@OptionsMenu(R.menu.action_save)
@EFragment(R.layout.category_details_fragment)
public class CategoryDetailsFragment extends Fragment {
    private enum DetailsType {ADD, EDIT}
    static final public String CATEGORY_ID = "CATEGORY_ID";

    @ViewById
    TextView categoryNameLabel;

    @ViewById
    EditText categoryName;

    @ViewById
    CheckBox isMainCategory;

    @ViewById
    TextView parentCategoryLabel;

    @ViewById
    Spinner parentCategory;

    @ViewById
    ToggleButton categoryIncomeType;

    @ViewById
    ToggleButton categoryExpenseType;

    @ViewById
    RadioGroup categoryTypes;

    @Inject @Named("local")
    CategoryDataManager localCategoryDataManager;

    private Long categoryId;
    private Category category;
    private DetailsType type;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        categoryId = getArguments().getLong(CATEGORY_ID);
        type = categoryId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        category = getCategory();
        return null;
    }

    @AfterViews
    void setupViews() {
        setupAdapters();
        setupListeners();
        fillViewsWithData();
    }

    private Category getCategory() {
        if (type.equals(DetailsType.EDIT)) {
            return localCategoryDataManager.findById(categoryId);
        }
        return new Category();
    }

    private void setupAdapters() {
        parentCategory.setAdapter(
                new ParentCategorySpinnerAdapter(
                        getActivity(),
                        android.R.layout.simple_spinner_item,
                        localCategoryDataManager.getMainCategories()
                )
        );
    }

    private void setupListeners() {
        isMainCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    parentCategoryLabel.setVisibility(View.INVISIBLE);
                    parentCategory.setVisibility(View.INVISIBLE);
                } else {
                    parentCategoryLabel.setVisibility(View.VISIBLE);
                    parentCategory.setVisibility(View.VISIBLE);
                }
            }
        });

        categoryIncomeType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                categoryTypesChecked((RadioGroup) compoundButton.getParent(), compoundButton.getId());
            }
        });

        categoryExpenseType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                categoryTypesChecked((RadioGroup) compoundButton.getParent(), compoundButton.getId());
            }
        });
    }

    private void categoryTypesChecked(RadioGroup radioGroup, int id) {
        int checked = 0;
        ToggleButton changed = null;

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            ToggleButton iteratedButton = (ToggleButton) radioGroup.getChildAt(i);
            if (iteratedButton.isChecked()) {
                checked++;
            }
            if (iteratedButton.getId() == id) {
                changed = iteratedButton;
            }
        }
        if (checked == 0) {
            changed.toggle();
        }
    }

    @OptionsItem(R.id.menu_save)
    void actionSave() {
        getDataFromViews();
        if (DetailsType.ADD.equals(type)) {
            localCategoryDataManager.insert(category);
        } else if (DetailsType.EDIT.equals(type)) {
            localCategoryDataManager.update(category);
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void fillViewsWithData() {
        if (type.equals(DetailsType.EDIT)) {
            categoryName.setText(category.getName());
            if (category.getParent() != null) {
                Category parent = localCategoryDataManager.findById(category.getParent().getId());
                isMainCategory.setChecked(false);
                parentCategory.setSelection(localCategoryDataManager.getMainCategories().indexOf(parent));
            }
            categoryIncomeType.setChecked(category.isIncomeType());
            categoryExpenseType.setChecked(category.isExpenseType());
        }
    }

    public void getDataFromViews() {
        category.setName(categoryName.getText().toString());

        if (categoryExpenseType.isChecked() && categoryIncomeType.isChecked()) {
            category.setType(Category.Type.INCOME_EXPENSE);
        } else if (categoryIncomeType.isChecked()) {
            category.setType(Category.Type.INCOME);
        } else if (categoryExpenseType.isChecked()) {
            category.setType(Category.Type.EXPENSE);
        }

        if (!isMainCategory.isChecked()) {
            category.setParent(((Category) parentCategory.getSelectedItem()));
        }
    }

    private class ParentCategorySpinnerAdapter extends ArrayAdapter<Category> {

        public ParentCategorySpinnerAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, null);
            }
            TextView categoryNameView = (TextView) convertView.findViewById(android.R.id.text1);
            categoryNameView.setText(getItem(position).getName());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, null);
            }
            TextView categoryNameView = (TextView) convertView.findViewById(android.R.id.text1);
            categoryNameView.setText(getItem(position).getName());
            return convertView;
        }

    }
}
