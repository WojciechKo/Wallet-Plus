package info.korzeniowski.walletplus.test.service.wallet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.WalletTypeCannotBeChangedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalWalletServiceTest {

    @Inject
    @Named("local")
    WalletService walletService;

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

    private Wallet getSimpleWallet(Wallet.Type type) {
        return new Wallet().setType(type).setName("Simple wallet-" + UUID.randomUUID()).setInitialAmount(11.1).setCurrentAmount(11.1);
    }
}
