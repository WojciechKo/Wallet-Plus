package info.korzeniowski.walletplus.test.service.wallet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.TestDatabaseModule;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalWalletServiceTest {

    @Inject
    WalletService walletService;

    @Inject
    CashFlowService cashFlowService;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).addModules(new TestDatabaseModule(Robolectric.application));
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldUpdateWalletData() {
        Long id = walletService.insert(getSimpleWallet().setName("oldName"));
        Wallet toUpdate = walletService.findById(id);

        String newName = "newName";
        walletService.update(toUpdate.setName(newName));

        Wallet updated = walletService.findById(id);
        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    public void shouldRemoveWallet() {
        Long myWalletId1 = walletService.insert(getSimpleWallet());
        Long myWalletId2 = walletService.insert(getSimpleWallet());

        assertThat(walletService.getAll()).hasSize(2);

        walletService.deleteById(myWalletId1);
        assertThat(walletService.getAll()).hasSize(1);

        walletService.deleteById(myWalletId2);
        assertThat(walletService.getAll()).hasSize(0);
    }

    @Test
    public void shouldDeleteRelatedCashFlowsAfterDelete() {
        walletService.insert(getSimpleWallet());
        walletService.insert(getSimpleWallet());
        walletService.insert(getSimpleWallet());
        walletService.insert(getSimpleWallet());

        cashFlowService.insert(getCashFlow(walletService.getAll().get(0), CashFlow.Type.INCOME));
        cashFlowService.insert(getCashFlow(walletService.getAll().get(0), CashFlow.Type.INCOME));
        cashFlowService.insert(getCashFlow(walletService.getAll().get(0), CashFlow.Type.EXPANSE));
        cashFlowService.insert(getCashFlow(walletService.getAll().get(1), CashFlow.Type.INCOME));

        long cashFlowCount = cashFlowService.count();

        walletService.deleteById(walletService.getAll().get(0).getId());

        assertThat(cashFlowService.count()).isEqualTo(cashFlowCount - 3);
    }

    private CashFlow getCashFlow(Wallet wallet, CashFlow.Type type) {
        return new CashFlow().setType(type).setAmount(50.00).setWallet(wallet).setDateTime(new Date());
    }

    private Wallet getSimpleWallet() {
        return new Wallet().setName("Simple wallet-" + UUID.randomUUID()).setInitialAmount(1000.0);
    }
}
