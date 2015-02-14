package info.korzeniowski.walletplus.ui.tag.details;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.text.MessageFormat;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.ui.BaseActivity;

public class TagDetailsActivity extends BaseActivity {
    public static final String TAG = TagDetailsActivity.class.getSimpleName();
    public static final String EXTRAS_TAG_ID = "TAG_ID";

    public static final int REQUEST_CODE_ADD_TAG = 601;
    public static final int REQUEST_CODE_EDIT_TAG = 602;
    public static final int RESULT_DELETED = 102;

    @Inject
    @Named("local")
    TagService localTagService;

    private Long tagId;
    private DetailsAction detailsAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_drawer);

        Bundle extras = getIntent().getExtras();
        tagId = extras == null ? -1 : extras.getLong(EXTRAS_TAG_ID);
        if (tagId == -1) {
            detailsAction = DetailsAction.ADD;
        } else {
            detailsAction = DetailsAction.EDIT;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, TagDetailsFragment.newInstance(tagId))
                    .commit();
        }

        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(TagDetailsActivity.this);
            }
        });
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
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return result;
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getConfirmationMessage())
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        localTagService.deleteById(tagId);
                        setResult(RESULT_DELETED);
                        finish();
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
        int count = (int) localTagService.countDependentCashFlows(tagId);
        String msg = getString(R.string.tagDeleteConfirmation);
        return MessageFormat.format(msg, count);
    }

    private enum DetailsAction {ADD, EDIT}
}
