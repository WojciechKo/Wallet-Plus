package info.korzeniowski.walletplus.test.service.cashflow;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.sql.SQLException;
import java.util.Date;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalCashFlowService;
import info.korzeniowski.walletplus.service.local.LocalCategoryService;
import info.korzeniowski.walletplus.service.local.LocalWalletService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCashFlowServiceTest {
    CashFlowService cashFlowService;
    WalletService walletService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        try {
            DatabaseHelper helper = new DatabaseHelper(Robolectric.application, null);
            cashFlowService = new LocalCashFlowService(helper.getCashFlowDao(), helper.getWalletDao());
            walletService = new LocalWalletService(helper.getWalletDao());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Setup failed.");
        }
    }

    @Test
    public void shouldFixRelatedWallets() {
        float amount = (float) 53.1;
        Wallet from = new Wallet.Builder().setName("from").setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).build();
        Wallet to = new Wallet.Builder().setName("to").setType(Wallet.Type.CONTRACTOR).setInitialAmount(0.0).build();
        walletService.insert(from);
        walletService.insert(to);
        double fromCurrentAmount = walletService.findById(from.getId()).getCurrentAmount();
        double toCurrentAmount = walletService.findById(to.getId()).getCurrentAmount();

        CashFlow cashFlow = new CashFlow.Builder()
                .setAmount(amount)
                .setDateTime(new Date())
                .setFromWallet(from)
                .setToWallet(to)
                .build();

        cashFlowService.insert(cashFlow);
        assertThat(walletService.findById(from.getId()).getCurrentAmount()).isEqualTo(fromCurrentAmount - amount);
        assertThat(walletService.findById(to.getId()).getCurrentAmount()).isEqualTo(toCurrentAmount + amount);
    }
}
