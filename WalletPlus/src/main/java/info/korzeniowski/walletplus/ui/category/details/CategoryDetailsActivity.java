package info.korzeniowski.walletplus.ui.category.details;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.BaseActivity;

public class CategoryDetailsActivity extends BaseActivity{
    public static final String CATEGORY_ID = "CATEGORY_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_drawer);

        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(CategoryDetailsActivity.this);
            }
        });

        if (null == savedInstanceState) {
            Bundle extras = getIntent().getExtras();
            Long categoryId = extras == null ? null : extras.getLong(CATEGORY_ID);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, CategoryDetailsFragment.newInstance(categoryId))
                    .commit();
        }
    }

    @Override
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.INVALID;
    }
}
