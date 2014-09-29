package info.korzeniowski.walletplus.test.service.wallet;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.service.local.validation.WalletValidator;

import static org.mockito.Mockito.mock;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private WalletService walletService;

    @Before
    public void setUp() {
        Dao<Wallet, Long> walletDao = mock(Dao.class);
        WalletService validatorService = mock(WalletService.class);
        walletService = new LocalWalletService(walletDao, new WalletValidator(validatorService));
    }

    @Test
    public void shouldNotInsertWalletWithoutType() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("Type");
        walletService.insert(new Wallet().setName("TestName").setInitialAmount(11.1));
    }

    @Test
    public void shouldInitialAmountBeDefinedInMyWallet() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("InitialAmount");
        walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setName("TestName"));
    }
}
