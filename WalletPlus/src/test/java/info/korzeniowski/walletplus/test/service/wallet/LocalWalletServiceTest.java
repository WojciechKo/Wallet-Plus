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
import info.korzeniowski.walletplus.service.exception.WalletTypeCannotBeChangedException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.failBecauseExceptionWasNotThrown;

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
    public void shouldThrowExceptionOnChangeWalletType() {
        Long id = walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        Wallet wallet = walletService.findById(id);

        try {
            walletService.update(wallet.setType(Wallet.Type.CONTRACTOR));
            failBecauseExceptionWasNotThrown(WalletTypeCannotBeChangedException.class);
        } catch (WalletTypeCannotBeChangedException e) {
            assertThat(walletService.count()).isEqualTo(1);
        }
    }

    @Test
    public void shouldAddDifferentTypeOfWallets() {
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));

        assertThat(walletService.getContractors()).hasSize(4);
        assertThat(walletService.getMyWallets()).hasSize(3);
        assertThat(walletService.getAll()).hasSize(7);
    }

    @Test
    public void shouldUpdateWalletData() {
        Long id = walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET).setName("oldName"));
        Wallet toUpdate = walletService.findById(id);

        String newName = "newName";
        walletService.update(toUpdate.setName(newName));

        Wallet updated = walletService.findById(id);
        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    public void shouldRemoveWallet() {
        Long myWalletId1 = walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        Long myWalletId2 = walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        Long contractorId = walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));

        walletService.deleteById(myWalletId1);
        assertThat(walletService.getContractors()).hasSize(1);
        assertThat(walletService.getMyWallets()).hasSize(1);
        assertThat(walletService.getAll()).hasSize(2);

        walletService.deleteById(contractorId);
        assertThat(walletService.getContractors()).hasSize(0);
        assertThat(walletService.getMyWallets()).hasSize(1);
        assertThat(walletService.getAll()).hasSize(1);

        walletService.deleteById(myWalletId2);
        assertThat(walletService.getContractors()).hasSize(0);
        assertThat(walletService.getMyWallets()).hasSize(0);
        assertThat(walletService.getAll()).hasSize(0);
    }

    @Test
    public void shouldInsertDuplicatedNamedWallet() {
        Wallet first = getSimpleWallet(Wallet.Type.MY_WALLET);
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET).setName(first.getName()));

        assertThat(walletService.getMyWallets()).hasSize(2);
        assertThat(walletService.getContractors()).hasSize(0);
    }

    @Test
    public void shouldDeleteRelatedCashFlowsAfterDelete() {
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));

        cashFlowService.insert(getCashFlow(walletService.getMyWallets().get(0), walletService.getContractors().get(0)));
        cashFlowService.insert(getCashFlow(walletService.getMyWallets().get(0), walletService.getContractors().get(1)));
        cashFlowService.insert(getCashFlow(walletService.getContractors().get(1), walletService.getMyWallets().get(0)));
        cashFlowService.insert(getCashFlow(walletService.getContractors().get(1), walletService.getMyWallets().get(1)));

        long cashFlowCount = cashFlowService.count();

        walletService.deleteById(walletService.getMyWallets().get(0).getId());

        assertThat(cashFlowService.count()).isEqualTo(cashFlowCount - 3);

        walletService.deleteById(walletService.getContractors().get(1).getId());

        assertThat(cashFlowService.count()).isEqualTo(cashFlowCount - 3 - 1);
    }

    private CashFlow getCashFlow(Wallet from, Wallet to) {
        return new CashFlow().setDateTime(new Date()).setAmount(50.00).setCategory(null).setFromWallet(from).setToWallet(to);
    }

    private Wallet getSimpleWallet(Wallet.Type type) {
        return new Wallet().setType(type).setName("Simple wallet-" + UUID.randomUUID()).setInitialAmount(11.1).setCurrentAmount(11.1);
    }
}
