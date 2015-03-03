package info.korzeniowski.walletplus.test.service.statistic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import javax.inject.Inject;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.TestDatabaseModule;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class StatisticServiceOrmLiteTest {

    @Inject
    CashFlowService cashFlowService;

    @Inject
    WalletService walletService;

    @Inject
    StatisticService statisticService;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).addModules(new TestDatabaseModule(Robolectric.application));
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldCountCashFlowsAssignedToWallet() {
        // given
        Wallet wallet1 = new Wallet().setName("Wallet 1").setInitialAmount(100.0);
        walletService.insert(wallet1);
        Wallet wallet2 = new Wallet().setName("Wallet 2").setInitialAmount(200.0);
        walletService.insert(wallet2);

        CashFlow cashFlow = new CashFlow()
                .setAmount(50.0)
                .setDateTime(new Date());

        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet1).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet1).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.EXPANSE));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.INCOME));

        // then
        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet1.getId())).isEqualTo(2);
        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet2.getId())).isEqualTo(3);
    }
}
