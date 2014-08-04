package info.korzeniowski.walletplus.test.robolectric.service;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.local.LocalCashFlowService;

import static org.mockito.Mockito.mock;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCashFlowServiceTest {
    CashFlowService cashFlowService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        Dao<CashFlow, Long> cashFlowDao = mock(Dao.class);
        Dao<Wallet, Long> walletDao = mock(Dao.class);
        cashFlowService = new LocalCashFlowService(cashFlowDao, walletDao);
    }

    @Test
    public void shouldDecreaseFromWallet() {
        float amount = (float) 53.1;
        Wallet from = new Wallet.Builder().setName("from").setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).build();
        Wallet to = new Wallet.Builder().setName("to").setType(Wallet.Type.CONTRACTOR).build();
        CashFlow cashFlow = new CashFlow(amount, new Date());
        cashFlow.setFromWallet(from);
        cashFlow.setToWallet(to);

        cashFlowService.insert(cashFlow);

    }

    @Test
    public void shouldIncreaseToWallet() {

    }
}
