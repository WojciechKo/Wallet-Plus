package info.korzeniowski.walletplus.test.ui.wallet.details;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MyRobolectricTestRunner;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Ignore
@RunWith(MyRobolectricTestRunner.class)
public class EditMyWalletTest {

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
    WalletService walletServiceMock;

    private Activity activity;
    private Wallet wallet;

    @Before
    public void setUp() {
        ((TestWalletPlus) RuntimeEnvironment.application).setMockComponent();
        ((TestWalletPlus) RuntimeEnvironment.application).component().inject(this);

        wallet = new Wallet().setId(47L).setName("Test wallet").setInitialAmount(100.0).setCurrentAmount(200.0);
        Mockito.when(walletServiceMock.findById(wallet.getId())).thenReturn(wallet);

        Intent intent = new Intent();
        intent.putExtra(WalletDetailsActivity.EXTRAS_WALLET_ID, wallet.getId());

        activity = Robolectric.buildActivity(WalletDetailsActivity.class).withIntent(intent).create().start().restart().get();
        ButterKnife.inject(this, activity);
    }

    @Test
    public void shouldCurrentAmountBeVisible() {
        assertThat(walletCurrentAmountLabel).isVisible();
        assertThat(walletCurrentAmount).isVisible();
    }

    @Test
    public void shouldShowsNameLabelAndValidateInput() {
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).isVisible();
        assertThat(walletName).hasNoError();
        assertThat(walletName).hasTextString(wallet.getName());

        walletName.setText("");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasError(R.string.walletNameIsRequired);

        walletName.setText("text");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasNoError();
    }

    @Test
    public void shouldInitialAmountLabelAppearsAfterTextInput() {
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        String previousCurrentAmount = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("0");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();
        assertThat(walletCurrentAmount).doesNotContainText(previousCurrentAmount);

        String currentAmountWhileInitialIsZero = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("100.0");
        assertThat(walletCurrentAmount).doesNotContainText(currentAmountWhileInitialIsZero);

        previousCurrentAmount = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("xxx");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsNotADigit);
        assertThat(walletCurrentAmount).hasTextString(previousCurrentAmount);

        previousCurrentAmount = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("12.2");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();
        assertThat(walletCurrentAmount).doesNotContainText(previousCurrentAmount);
    }

    @Test
    public void shouldCurrentAmountChangeAfterChangeInitialAmount() {
        String currentAmountString = walletCurrentAmount.getText().toString().replaceAll("[^\\d.]+", "");
        Double currentAmount = Double.parseDouble(currentAmountString);
        int difference = 200;

        Double newInitialAmount = wallet.getInitialAmount() + difference;
        walletInitialAmount.setText(newInitialAmount.toString());

        String newCurrentAmountString = walletCurrentAmount.getText().toString().replaceAll("[^\\d.]+", "");
        Double newCurrentAmount = Double.parseDouble(newCurrentAmountString);
        assertThat(newCurrentAmount).isEqualTo(currentAmount + difference);
    }

    @Test
    public void shouldCallUpdateWallet() {
        walletInitialAmount.setText("150.0");
        walletName.setText("newTextName");
        Wallet toUpdate = new Wallet()
                .setId(wallet.getId())
                .setName(walletName.getText().toString())
                .setInitialAmount(Double.parseDouble(walletInitialAmount.getText().toString()))
                .setCurrentAmount(wallet.getCurrentAmount());

        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_save));

        Mockito.verify(walletServiceMock, Mockito.times(1)).update(toUpdate);
    }

    @Test
    public void shouldNotCallUpdateWhenErrors() {
        walletName.setError("simple error");
        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_save));
        Mockito.verify(walletServiceMock, Mockito.never()).update(Mockito.any(Wallet.class));

        walletName.setError(null);
        walletInitialAmount.setError("other error");
        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_save));
        Mockito.verify(walletServiceMock, Mockito.never()).update(Mockito.any(Wallet.class));
    }
}
