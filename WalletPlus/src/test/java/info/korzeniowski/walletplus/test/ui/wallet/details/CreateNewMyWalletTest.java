package info.korzeniowski.walletplus.test.ui.wallet.details;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.test.module.MockDatabaseModule;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;

import static org.fest.assertions.api.ANDROID.assertThat;


@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CreateNewMyWalletTest {

    @InjectView(R.id.walletNameLabel)
    TextView walletNameLabel;

    @InjectView(R.id.walletName)
    EditText walletName;

    @InjectView(R.id.walletInitialAmountLabel)
    TextView walletInitialAmountLabel;

    @InjectView(R.id.walletInitialAmount)
    EditText walletInitialAmount;

    @InjectView(R.id.walletCurrentAmountLabel)
    TextView walletCurrentAmountLabel;

    @InjectView(R.id.walletCurrentAmount)
    TextView walletCurrentAmount;

    @Inject
    @Named("local")
    WalletService walletService;

    private Activity activity;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).removeModule(TestDatabaseModule.class);
        ((TestWalletPlus) Robolectric.application).addModules(new MockDatabaseModule());
        ((TestWalletPlus) Robolectric.application).inject(this);

        activity = Robolectric.buildActivity(WalletDetailsActivity.class).create().start().restart().get();
        ButterKnife.inject(this, activity);
    }

//    TODO: change it to check textValue in dialog.
//    @Test
//    public void testConfirmationMessage() {
//        assertThat(MessageFormat.format(message, 0)).isEqualTo("Do you want to delete this wallet?");
//        assertThat(MessageFormat.format(message, 1)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 1 cashflow.");
//        assertThat(MessageFormat.format(message, 2)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 2 cashflows.");
//    }


    @Test
    public void shouldCurrentAmountBeInvisible() {
        assertThat(walletCurrentAmountLabel).isGone();
        assertThat(walletCurrentAmount).isGone();
    }

    @Test
    public void shouldNameLabelAppearsAfterTextInput() {
        assertThat(walletNameLabel).isInvisible();
        assertThat(walletName).isVisible();
        assertThat(walletName).hasNoError();

        walletName.setText("test");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasNoError();

        walletName.setText("");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasError(R.string.walletNameIsRequired);

        walletName.setText("text");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasNoError();
    }

    @Test
    public void shouldInitialAmountLabelAppearsAfterTextInput() {
        assertThat(walletInitialAmountLabel).isInvisible();
        assertThat(walletInitialAmount).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        walletInitialAmount.setText("1.21");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        walletInitialAmount.setText("xxx");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsNotADigit);

        walletInitialAmount.setText("12.2");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();
    }


    @Test @Ignore
    public void shouldCallUpdateWallet() {
        walletInitialAmount.setText("150.0");
        walletName.setText("textName");
        Wallet toInsert = new Wallet()
                .setName(walletName.getText().toString())
                .setInitialAmount(Double.parseDouble(walletInitialAmount.getText().toString()));

        activity.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));

        Mockito.verify(walletService, Mockito.times(1)).insert(toInsert);
    }

    @Test @Ignore
    public void shouldNotCallUpdateWhenErrors() {
        walletName.setError("simple error");
        activity.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));
        Mockito.verify(walletService, Mockito.never()).insert(Mockito.any(Wallet.class));

        walletName.setError(null);
        walletInitialAmount.setError("other error");
        activity.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));
        Mockito.verify(walletService, Mockito.never()).insert(Mockito.any(Wallet.class));
    }
}
