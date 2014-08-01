package info.korzeniowski.walletplus.test.robolectric.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.service.AccountService;
import info.korzeniowski.walletplus.service.local.LocalAccountService;
import info.korzeniowski.walletplus.model.Account;

import static org.fest.assertions.api.Assertions.assertThat;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalAccountServiceTest {

    private AccountService accountService;

    @Before
    public void setUp() {
        accountService = new LocalAccountService(null);
    }

    @Ignore
    @Test
    public void shouldCreateNewAccount() {

        Account account = new Account();
        account.setName("MyAccount");
        account.setPasswordHash("q1w2e3r4");

        accountService.insert(account);

        Account read = accountService.findById(account.getId());
        assertThat(accountService.count()).isEqualTo(1);
        assertThat(read).isEqualTo(account);
        assertThat(read.getId()).isEqualTo(account.getId());
        assertThat(read.getName()).isEqualTo(account.getName());
        assertThat(read.getPasswordHash()).isEqualTo(account.getPasswordHash());
    }

}
