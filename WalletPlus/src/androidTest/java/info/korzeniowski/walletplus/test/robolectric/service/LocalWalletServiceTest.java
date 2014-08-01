package info.korzeniowski.walletplus.test.robolectric.service;

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

import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.WalletNameAndTypeMustBeUniqueException;
import info.korzeniowski.walletplus.service.exception.WalletTypeCannotBeChangedException;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalWalletService;
import info.korzeniowski.walletplus.model.Wallet;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.fest.assertions.api.Fail.fail;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalWalletServiceTest {
    private WalletService walletService;
    private DatabaseHelper databaseHelper;

    @Before
    public void setUp() {
        databaseHelper = OpenHelperManager.getHelper(Robolectric.application, DatabaseHelper.class);
        try {
            if (walletService == null) {
                walletService = new LocalWalletService(databaseHelper.getWalletDao());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail("setUp fail.");
        }
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
        String oldName = "oldName";
        String newName = "newName";
        Long id = walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET).setName(oldName));
        Wallet toUpdate = walletService.findById(id);

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
    public void shouldNotInsertDuplicatedNamedAndTypedWallet() {
        Wallet first = getSimpleWallet(Wallet.Type.MY_WALLET);
        Integer myWalletsBefore = walletService.getMyWallets().size();

        walletService.insert(first);
        try {
            walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET).setName(first.getName()));
            failBecauseExceptionWasNotThrown(WalletNameAndTypeMustBeUniqueException.class);
        } catch (WalletNameAndTypeMustBeUniqueException e) {
            assertThat(walletService.getMyWallets()).hasSize(myWalletsBefore + 1);
        }
    }

    @Test
    public void shouldInsertDuplicatedNamedButDifferentTypedWallet() {
        Wallet first = getSimpleWallet(Wallet.Type.MY_WALLET);
        List<Wallet> all = walletService.getAll();
        walletService.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletService.insert(getSimpleWallet(Wallet.Type.CONTRACTOR).setName(first.getName()));

        assertThat(walletService.getMyWallets()).hasSize(1);
        assertThat(walletService.getContractors()).hasSize(1);
    }

    private Wallet getSimpleWallet(Wallet.Type type) {
        return new Wallet().setType(type).setName("Simple wallet-" + System.currentTimeMillis()).setInitialAmount(11.1).setCurrentAmount(11.1);
    }
}
