package info.korzeniowski.walletplus.test.service.wallet;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.WalletNameAndTypeMustBeUniqueException;
import info.korzeniowski.walletplus.service.exception.WalletTypeCannotBeChangedException;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.model.Wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalWalletServiceTest {
    private WalletService walletService;

    @Before
    public void setUp() {
        try {
            DatabaseHelper helper = new DatabaseHelper(Robolectric.application, null);
            walletService = new LocalWalletService(helper.getWalletDao());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Setup failed.");
        }
    }

    @Test
    public void shouldThrowExceptionOnChangeWalletType() {
        Long id = walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build());
        Wallet wallet = walletService.findById(id);
        try {
            walletService.update(new Wallet.Builder(wallet).setType(Wallet.Type.CONTRACTOR).build());
            failBecauseExceptionWasNotThrown(WalletTypeCannotBeChangedException.class);
        } catch (WalletTypeCannotBeChangedException e) {
            assertThat(walletService.count()).isEqualTo(1);
        }
    }

    @Test
    public void shouldAddDifferentTypeOfWallets() {
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build());
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.CONTRACTOR).build());
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.CONTRACTOR).build());
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build());
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.CONTRACTOR).build());
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.CONTRACTOR).build());
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build());

        assertThat(walletService.getContractors()).hasSize(4);
        assertThat(walletService.getMyWallets()).hasSize(3);
        assertThat(walletService.getAll()).hasSize(7);
    }

    @Test
    public void shouldUpdateWalletData() {
        String oldName = "oldName";
        String newName = "newName";
        Long id = walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).setName(oldName).build());
        Wallet toUpdate = walletService.findById(id);

        walletService.update(new Wallet.Builder(toUpdate).setName(newName).build());
        Wallet updated = walletService.findById(id);

        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    public void shouldRemoveWallet() {
        Long myWalletId1 = walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build());
        Long myWalletId2 = walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build());
        Long contractorId = walletService.insert(getSimpleWalletBuilder(Wallet.Type.CONTRACTOR).build());

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
    public void shouldNotInsertDuplicatedNamedAndTypedWallet() {
        Wallet first = getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build();
        Integer myWalletsBefore = walletService.getMyWallets().size();

        walletService.insert(first);
        try {
            walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).setName(first.getName()).build());
            failBecauseExceptionWasNotThrown(WalletNameAndTypeMustBeUniqueException.class);
        } catch (WalletNameAndTypeMustBeUniqueException e) {
            assertThat(walletService.getMyWallets()).hasSize(myWalletsBefore + 1);
        }
    }

    @Test
    public void shouldInsertDuplicatedNamedButDifferentTypedWallet() {
        Wallet first = getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build();
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.MY_WALLET).build());
        walletService.insert(getSimpleWalletBuilder(Wallet.Type.CONTRACTOR).setName(first.getName()).build());

        assertThat(walletService.getMyWallets()).hasSize(1);
        assertThat(walletService.getContractors()).hasSize(1);
    }

    private Wallet.Builder getSimpleWalletBuilder(Wallet.Type type) {
        return new Wallet.Builder().setType(type).setName("Simple wallet-" + UUID.randomUUID()).setInitialAmount(11.1).setCurrentAmount(11.1);
    }
}
