package info.korzeniowski.walletplus.test.ui.wallet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.MessageFormat;

import info.korzeniowski.walletplus.R;

import static org.assertj.core.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletFragmentTest {

    @Test
    public void testConfirmationMessage() {
        String message = Robolectric.application.getResources().getString(R.string.walletDeleteConfirmation);

        assertThat(MessageFormat.format(message, 0)).isEqualTo("Do you want to delete this wallet?");
        assertThat(MessageFormat.format(message, 1)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 1 cashflow.");
        assertThat(MessageFormat.format(message, 2)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 2 cashflows.");
    }
}
