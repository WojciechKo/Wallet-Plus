package info.korzeniowski.walletplus.ui.wallets.details;

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
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.ui.BaseActivity;

public class WalletDetailsActivity extends BaseActivity {
    public static final String TAG = WalletDetailsActivity.class.getSimpleName();
    public static final String EXTRAS_WALLET_ID = "WALLET_ID";

    public static final int REQUEST_CODE_ADD_WALLET = 601;
    public static final int REQUEST_CODE_EDIT_WALLET = 602;
    public static final int RESULT_DELETED = 102;

    @Inject
    WalletService walletService;

    private Long walletId;
    private DetailsAction detailsAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_drawer);

        Bundle extras = getIntent().getExtras();
        walletId = extras == null ? -1 : extras.getLong(EXTRAS_WALLET_ID);
        if (walletId == -1) {
            detailsAction = DetailsAction.ADD;
        } else {
            detailsAction = DetailsAction.EDIT;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, WalletDetailsFragment.newInstance(walletId))
                    .commit();
        }

        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(WalletDetailsActivity.this);
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
                        walletService.deleteById(walletId);
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
        int count = (int) walletService.countDependentCashFlows(walletId);
        String msg = getString(R.string.walletDeleteConfirmation);
        return MessageFormat.format(msg, count);
    }

    private enum DetailsAction {ADD, EDIT}
}
