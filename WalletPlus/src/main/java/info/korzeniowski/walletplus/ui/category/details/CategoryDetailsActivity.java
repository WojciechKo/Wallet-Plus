package info.korzeniowski.walletplus.ui.category.details;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.ui.BaseActivity;

public class CategoryDetailsActivity extends BaseActivity {
    public static final String TAG = CategoryDetailsActivity.class.getSimpleName();
    public static final String EXTRAS_CATEGORY_ID = "CATEGORY_ID";

    public static final int REQUEST_CODE_NEW_CATEGORY = 501;

    public static final int REQUEST_CODE_SHOW_DETAILS = 502;
    public static final int RESULT_DELETED = 101;
    public static final String RESULT_DATA_DELETED_CATEGORY_ID = "DELETED_CATEGORY_ID";

    @InjectView(R.id.categoryNameLabel)
    TextView categoryNameLabel;

    @InjectView(R.id.categoryName)
    EditText categoryName;

    @InjectView(R.id.parentCategory)
    Spinner parentCategoryView;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    private DetailsAction detailsAction;
    private List<Category> parentCategoryList;
    private Category category;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_category_details);
        ButterKnife.inject(this);

        Bundle extras = getIntent().getExtras();
        Long categoryId = extras == null ? -1 : extras.getLong(EXTRAS_CATEGORY_ID);
        initState(categoryId);

        setupViews();

        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(CategoryDetailsActivity.this);
            }
        });
    }

    private void initState(Long categoryId) {
        if (categoryId == -1) {
            category = new Category();
            detailsAction = DetailsAction.ADD;
        } else {
            category = localCategoryService.findById(categoryId);
            detailsAction = DetailsAction.EDIT;
        }
    }

    void setupViews() {
        setupAdapters();
        fillViewsWithData();
    }

    private void setupAdapters() {
        parentCategoryList = localCategoryService.getMainCategories();
        parentCategoryView.setAdapter(new ParentCategoryAdapter(this, parentCategoryList));
    }

    private void fillViewsWithData() {
        categoryName.setText(category.getName());
        if (category.getParent() != null) {
            Category parentCategory = localCategoryService.findById(category.getParent().getId());
            parentCategoryView.setSelection(parentCategoryList.indexOf(parentCategory));
        }
    }

    @OnItemSelected(R.id.parentCategory)
    public void onParentCategorySelected(AdapterView<?> parentView, int position) {
        if (position == 0) {
            category.setParent(null);
        } else {
            Category selectedParent = (Category) parentView.getSelectedItem();
            category.setParent(selectedParent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (detailsAction == DetailsAction.EDIT) {
            getMenuInflater().inflate(R.menu.action_delete, menu);
        }
        getMenuInflater().inflate(R.menu.action_save, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            onDeleteOptionSelected();
            return true;
        } else if (item.getItemId() == R.id.menu_save) {
            onSaveOptionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onDeleteOptionSelected() {
        if (category.getChildren().isEmpty()) {
            showDeleteConfirmationAlert();
        } else {
            Toast.makeText(this, getString(R.string.categoryCantDeleteCategoryWithSubs), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmationAlert() {
        new AlertDialog.Builder(this)
                .setMessage(getConfirmationMessage())
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryDeleteCategory(category);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private String getConfirmationMessage() {
        Integer count = localCashFlowService.findCashFlow(null, null, category.getId(), null, null).size();
        StringBuilder sb = new StringBuilder("Do you want to delete category:\n" + category.getName());
        if (count > 0) {
            sb.append("\n\n" + count + " cashflows will be uncategorized.");
        }
        return sb.toString();
    }

    private void tryDeleteCategory(Category category) {
        try {
            localCategoryService.deleteById(category.getId());
            Intent data = new Intent();
            data.putExtra(RESULT_DATA_DELETED_CATEGORY_ID, category.getId());
            setResult(RESULT_DELETED, data);
            finish();
        } catch (CategoryHaveSubsException e) {
            Toast.makeText(this, R.string.categoryCantDeleteCategoryWithSubs, Toast.LENGTH_SHORT).show();
        }
    }

    void onSaveOptionSelected() {
        if (Strings.isNullOrEmpty(categoryName.getText().toString())) {
            categoryName.setError(getString(R.string.categoryMustHaveName));
            return;
        }

        category.setName(categoryName.getText().toString().trim());

        if (detailsAction == DetailsAction.ADD) {
            localCategoryService.insert(category);
            setResult(RESULT_OK);
            finish();
        } else if (detailsAction == DetailsAction.EDIT) {
            try {
                localCategoryService.update(category);
                finish();
            } catch (CategoryHaveSubsException e) {
                Toast.makeText(this, R.string.categorySubCantHaveSubs, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.INVALID;
    }

    private enum DetailsAction {ADD, EDIT}

    class ParentCategoryAdapter extends BaseAdapter {
        private final Context context;
        private final List<Category> mainCategories;

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

        @Override
        public Category getItem(int position) {
            return mainCategories.get(position);
        }

        class ParentCategoryViewHolder {
            @InjectView(android.R.id.text1)
            TextView categoryName;
        }
    }
}
