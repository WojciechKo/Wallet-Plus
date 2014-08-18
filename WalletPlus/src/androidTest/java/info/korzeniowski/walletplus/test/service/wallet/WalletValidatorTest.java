package info.korzeniowski.walletplus.test.service.wallet;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.service.local.validation.WalletValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletValidatorTest {
    WalletService walletService;
    private WalletService validatorService;

    @Before
    public void setUp() {
        Dao<Wallet, Long> walletDao = mock(Dao.class);
        validatorService = mock(WalletService.class);
        walletService = new LocalWalletService(walletDao, new WalletValidator(validatorService));
    }

    @Test
    public void walletMustHaveType() {
        try {
            walletService.insert(new Wallet.Builder().setName("TestName").setInitialAmount(11.1).build());
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeNullOrEmptyException.class);
        } catch (EntityPropertyCannotBeNullOrEmptyException e) {
            assertThat(e.getProperty().equals("Type"));
        }
    }

    @Test
    public void myWalletShouldHaveInitialAmount() {
        try {
            walletService.insert(new Wallet.Builder().setType(Wallet.Type.MY_WALLET).setName("TestName").build());
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeNullOrEmptyException.class);
        } catch (EntityPropertyCannotBeNullOrEmptyException e) {
            assertThat(e.getProperty().equals("InitialAmount"));
        }
    }
}
