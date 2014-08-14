package info.korzeniowski.walletplus.test.service.wallet;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.sql.SQLException;

import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.test.service.TestDatabaseHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletServiceTest {

    WalletService walletService;

    @Before
    public void setUp() throws SQLException {
        DatabaseHelper helper = new TestDatabaseHelper(Robolectric.application);
        Dao<Wallet, Long> walletDao = helper.getWalletDao();
        walletService = new LocalWalletService(walletDao);
    }

    @Test
    public void testInsert() {
        assertThat(walletService.getAll().size()).isEqualTo(0);
        walletService.insert(new Wallet.Builder().setName("Wallet name").setInitialAmount(1.2).setType(Wallet.Type.CONTRACTOR).build());
        assertThat(walletService.getAll().size()).isEqualTo(1);
        assertThat(walletService.getAll().get(0).getName()).isEqualTo("Wallet name");
    }

}
