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
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalWalletServiceTest {

    @Inject
    @Named("local")
    WalletService walletService;

    @Inject
    @Named("local")
    CashFlowService cashFlowService;

    @Before
    public void setUp() {
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

        assertThat(walletService.getMyWallets()).hasSize(2);

        walletService.deleteById(myWalletId1);
        assertThat(walletService.getMyWallets()).hasSize(1);

        walletService.deleteById(myWalletId2);
        assertThat(walletService.getMyWallets()).hasSize(0);
    }

    @Test
    public void shouldDeleteRelatedCashFlowsAfterDelete() {
        walletService.insert(getSimpleWallet());
        walletService.insert(getSimpleWallet());
        walletService.insert(getSimpleWallet());
        walletService.insert(getSimpleWallet());

        cashFlowService.insert(getCashFlow(walletService.getMyWallets().get(0)));
        cashFlowService.insert(getCashFlow(walletService.getMyWallets().get(0)));
        cashFlowService.insert(getCashFlow(walletService.getMyWallets().get(0)));
        cashFlowService.insert(getCashFlow(walletService.getMyWallets().get(1)));

        long cashFlowCount = cashFlowService.count();

        walletService.deleteById(walletService.getMyWallets().get(0).getId());

        assertThat(cashFlowService.count()).isEqualTo(cashFlowCount - 3);

        assertThat(cashFlowService.count()).isEqualTo(cashFlowCount - 3 - 1);
    }

    private CashFlow getCashFlow(Wallet from) {
        return new CashFlow().setDateTime(new Date()).setAmount(50.00).setCategory(null).setWallet(from);
    }

    private Wallet getSimpleWallet() {
        return new Wallet().setName("Simple wallet-" + UUID.randomUUID()).setInitialAmount(11.1).setCurrentAmount(11.1);
    }
}
