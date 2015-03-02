package info.korzeniowski.walletplus.test.service.wallet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.service.local.validation.WalletValidator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletValidatorTest {

    @Inject
    @Named("local")
    WalletService walletService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).inject(this);
        ((LocalWalletService) walletService).setWalletValidator(new WalletValidator(mock(WalletService.class)));
    }

    @Test
    public void fakeTest() {
        assertThat(true).isTrue();
    }
}
