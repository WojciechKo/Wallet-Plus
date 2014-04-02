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

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.model.Category;

@EFragment(R.layout.category_details_fragment)
@OptionsMenu(R.menu.action_save)
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

    @Inject
    LocalCategoryDataManager localCategoryDataManager;

    private long categoryId;
    private Category category;
    private DetailsType type;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        categoryId = getArguments().getLong(CATEGORY_ID);
        category = getCategory();
        type = Category.INVALID_ID.equals(categoryId) ? DetailsType.ADD : DetailsType.EDIT;
        return null;
    }

    @AfterViews
    void setupViews() {
        setupAdapters();
        setupListeners();
        if (type.equals(DetailsType.ADD)) {
            category = new Category();
        } else if (type.equals(DetailsType.EDIT)) {
            fillViewsWithData();
        }
    }

    private Category getCategory() {
        return localCategoryDataManager.getById(categoryId);
    }

    private void setupAdapters() {
        parentCategory.setAdapter(
                new ParentSpinnerAdapter(
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
        Category category = localCategoryDataManager.getById(categoryId);
        categoryName.setText(category.getName());
        if (category.getParent() != null) {
            Category parent = localCategoryDataManager.getById(category.getParentId());
            isMainCategory.setChecked(false);
            parentCategory.setSelection(localCategoryDataManager.getMainCategories().indexOf(parent));
        }
        categoryIncomeType.setChecked(category.getTypes().contains(Category.Type.INCOME));
        categoryExpenseType.setChecked(category.getTypes().contains(Category.Type.EXPENSE));
    }

    public void getDataFromViews() {
        category.setName(categoryName.getText().toString());

        EnumSet categorySet = EnumSet.noneOf(Category.Type.class);
        if (categoryIncomeType.isChecked()) {
            categorySet.add(Category.Type.INCOME);
        }
        if (categoryExpenseType.isChecked()) {
            categorySet.add(Category.Type.EXPENSE);
        }
        category.setTypes(categorySet);

        if (!isMainCategory.isChecked()) {
            category.setParentId(((Category) parentCategory.getSelectedItem()).getId());
        }
    }

    private class ParentSpinnerAdapter extends ArrayAdapter<Category> {

        public ParentSpinnerAdapter(Context context, int resource, List<Category> objects) {
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
