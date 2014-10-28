package info.korzeniowski.walletplus.test.ui.wallet;

import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;
import org.robolectric.util.FragmentTestUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.ui.wallet.details.WalletDetailsFragment;

import static org.fest.assertions.api.ANDROID.assertThat;


@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletFragmentTest {

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

    @Before
    public void setUp() {
        Fragment fragment = new WalletDetailsFragment();
        FragmentTestUtil.startFragment(fragment);
        ActivityController.of(fragment.getActivity()).visible();
        ButterKnife.inject(this, fragment.getView());
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
        assertThat(walletCurrentAmountLabel).isInvisible();
        assertThat(walletCurrentAmount).isInvisible();

    }

    @Test
    public void shouldWalletNameLabelAppearsAfterTextInput() {
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
    public void shouldWalletInitialAmountLabelAppearsAfterTextInput() {
        assertThat(walletInitialAmountLabel).isInvisible();
        assertThat(walletInitialAmount).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        walletInitialAmount.setText("1.21");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        walletInitialAmount.setText("");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsRequired);
    }
}
