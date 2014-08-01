package info.korzeniowski.walletplus.test.robolectric.service.validator;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.sql.SQLException;

import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.model.Wallet;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletValidatorTest {
    WalletService walletService;

    @Before
    public void setUp() {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(Robolectric.application, DatabaseHelper.class);
        try {
            walletService = new LocalWalletService(databaseHelper.getWalletDao());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void walletMustHaveType() {
        try {
            walletService.insert(new Wallet().setName("TestName").setInitialAmount(11.1));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeNullOrEmptyException.class);
        } catch (EntityPropertyCannotBeNullOrEmptyException e) {
            assertThat(e.getProperty().equals("Type"));
        }
    }

    @Test
    public void myWalletShouldHaveInitialAmount() {
        try {
            walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setName("TestName"));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeNullOrEmptyException.class);
        } catch (EntityPropertyCannotBeNullOrEmptyException e) {
            assertThat(e.getProperty().equals("InitialAmount"));
        }
    }
}
