package info.korzeniowski.walletplus.test.robolectric.service.validator;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.test.TestDatabaseHelper;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletTest {

    WalletService walletService;

    @Before
    public void setUp() throws SQLException {
        DatabaseHelper helper = new TestDatabaseHelper(Robolectric.application);
        Dao<Wallet, Long> walletDao = helper.getWalletDao();
        walletService = new LocalWalletService(walletDao);
    }

    @Test
    public void testMy() {
        String message = Robolectric.application.getResources().getString(R.string.walletDeleteConfirmation);

        assertThat(MessageFormat.format(message, 0)).isEqualTo("Do you want to delete this wallet?");
        assertThat(MessageFormat.format(message, 1)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 1 cashflow.");
        assertThat(MessageFormat.format(message, 2)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 2 cashflows.");
    }

    @Test
    public void testSth() {
        walletService.insert(new Wallet.Builder().setName("Test name").setInitialAmount(1.2).setType(Wallet.Type.MY_WALLET).build());
        assertThat(walletService.getAll().size()).isEqualTo(1);
    }
}
