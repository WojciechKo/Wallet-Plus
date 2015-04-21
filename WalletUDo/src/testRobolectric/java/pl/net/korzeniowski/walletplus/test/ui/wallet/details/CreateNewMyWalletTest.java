package pl.net.korzeniowski.walletplus.test.ui.wallet.details;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.fakes.RoboMenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.net.korzeniowski.walletplus.MyRobolectricTestRunner;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.TestWalletPlus;
import com.walletudo.model.Wallet;
import com.walletudo.service.WalletService;
import com.walletudo.ui.wallets.details.WalletDetailsActivity;

import static org.fest.assertions.api.ANDROID.assertThat;

//@Ignore
@RunWith(MyRobolectricTestRunner.class)
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
    WalletService walletServiceMock;

    private Activity activity;

    @Before
    public void setUp() {
        ((TestWalletPlus) RuntimeEnvironment.application).setMockComponent();
        ((TestWalletPlus) RuntimeEnvironment.application).component().inject(this);

        activity = Robolectric.buildActivity(WalletDetailsActivity.class).create().start().restart().get();
        ButterKnife.inject(this, activity);
    }

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


    @Test
    public void shouldCallUpdateWallet() {
        walletInitialAmount.setText("150.0");
        walletName.setText("textName");
        Wallet toInsert = new Wallet()
                .setName(walletName.getText().toString())
                .setInitialAmount(Double.parseDouble(walletInitialAmount.getText().toString()));

        Shadows.shadowOf(activity).clickMenuItem(R.id.menu_save);

        Mockito.verify(walletServiceMock, Mockito.times(1)).insert(toInsert);
    }

    @Test
    public void shouldNotCallUpdateWhenErrors() {
        walletName.setError("simple error");
        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_save));
        Mockito.verify(walletServiceMock, Mockito.never()).insert(Mockito.any(Wallet.class));

        walletName.setError(null);
        walletInitialAmount.setError("other error");
        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_save));
        Mockito.verify(walletServiceMock, Mockito.never()).insert(Mockito.any(Wallet.class));
    }
}
