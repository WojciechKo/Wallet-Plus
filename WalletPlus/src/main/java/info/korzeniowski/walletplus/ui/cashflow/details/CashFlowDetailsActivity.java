package info.korzeniowski.walletplus.ui.cashflow.details;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.ui.BaseActivity;

public class CashFlowDetailsActivity extends BaseActivity {
    public static final String EXTRAS_CASH_FLOW_ID = "CASH_FLOW_ID";

    public static final int REQUEST_CODE_ADD_CASH_FLOW = 401;
    public static final int REQUEST_CODE_EDIT_CASH_FLOW = 402;
    public static final int RESULT_DELETED = 103;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    private Long cashFlowId;
    private DetailsAction detailsAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_drawer);

        Bundle extras = getIntent().getExtras();
        cashFlowId = extras == null ? -1 : extras.getLong(EXTRAS_CASH_FLOW_ID);
        if (cashFlowId == -1) {
            detailsAction = DetailsAction.ADD;
        } else {
            detailsAction = DetailsAction.EDIT;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, CashFlowDetailsFragment.newInstance(cashFlowId))
                    .commit();
        }

        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(CashFlowDetailsActivity.this);
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
                .setMessage(getString(R.string.cashFlowDeleteConfirmation))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        localCashFlowService.deleteById(cashFlowId);
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

    private enum DetailsAction {ADD, EDIT}
}
