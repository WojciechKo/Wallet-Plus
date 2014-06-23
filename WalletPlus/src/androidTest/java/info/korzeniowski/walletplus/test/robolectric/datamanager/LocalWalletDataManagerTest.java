package info.korzeniowski.walletplus.test.robolectric.datamanager;

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

import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.exception.WalletNameAndTypeMustBeUniqueException;
import info.korzeniowski.walletplus.datamanager.exception.WalletTypeCannotBeChangedException;
import info.korzeniowski.walletplus.datamanager.local.DatabaseHelper;
import info.korzeniowski.walletplus.datamanager.local.LocalWalletDataManager;
import info.korzeniowski.walletplus.model.Wallet;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.fest.assertions.api.Fail.fail;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalWalletDataManagerTest {
    private WalletDataManager walletDataManager;
    private DatabaseHelper databaseHelper;

    @Before
    public void setUp() {
        databaseHelper = OpenHelperManager.getHelper(Robolectric.application, DatabaseHelper.class);
        try {
            if (walletDataManager == null) {
                walletDataManager = new LocalWalletDataManager(databaseHelper.getWalletDao());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail("setUp fail.");
        }
    }

    @Test
    public void shouldThrowExceptionOnChangeWalletType() {
        Long id = walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        Wallet wallet = walletDataManager.findById(id);
        try {
            walletDataManager.update(wallet.setType(Wallet.Type.CONTRACTOR));
            failBecauseExceptionWasNotThrown(WalletTypeCannotBeChangedException.class);
        } catch (WalletTypeCannotBeChangedException e) {
            assertThat(walletDataManager.count()).isEqualTo(1);
        }
    }

    @Test
    public void shouldAddDifferentTypeOfWallets() {
        walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletDataManager.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletDataManager.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletDataManager.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletDataManager.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));
        walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET));

        assertThat(walletDataManager.getContractors()).hasSize(4);
        assertThat(walletDataManager.getMyWallets()).hasSize(3);
        assertThat(walletDataManager.getAll()).hasSize(7);
    }

    @Test
    public void shouldUpdateWalletData() {
        String oldName = "oldName";
        String newName = "newName";
        Long id = walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET).setName(oldName));
        Wallet toUpdate = walletDataManager.findById(id);

        walletDataManager.update(toUpdate.setName(newName));
        Wallet updated = walletDataManager.findById(id);

        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    public void shouldRemoveWallet() {
        Long myWalletId1 = walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        Long myWalletId2 = walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        Long contractorId = walletDataManager.insert(getSimpleWallet(Wallet.Type.CONTRACTOR));

        walletDataManager.deleteById(myWalletId1);
        assertThat(walletDataManager.getContractors()).hasSize(1);
        assertThat(walletDataManager.getMyWallets()).hasSize(1);
        assertThat(walletDataManager.getAll()).hasSize(2);

        walletDataManager.deleteById(contractorId);
        assertThat(walletDataManager.getContractors()).hasSize(0);
        assertThat(walletDataManager.getMyWallets()).hasSize(1);
        assertThat(walletDataManager.getAll()).hasSize(1);

        walletDataManager.deleteById(myWalletId2);
        assertThat(walletDataManager.getContractors()).hasSize(0);
        assertThat(walletDataManager.getMyWallets()).hasSize(0);
        assertThat(walletDataManager.getAll()).hasSize(0);
    }

    @Test
    public void shouldNotInsertDuplicatedNamedAndTypedWallet() {
        Wallet first = getSimpleWallet(Wallet.Type.MY_WALLET);
        Integer myWalletsBefore = walletDataManager.getMyWallets().size();

        walletDataManager.insert(first);
        try {
            walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET).setName(first.getName()));
            failBecauseExceptionWasNotThrown(WalletNameAndTypeMustBeUniqueException.class);
        } catch (WalletNameAndTypeMustBeUniqueException e) {
            assertThat(walletDataManager.getMyWallets()).hasSize(myWalletsBefore + 1);
        }
    }

    @Test
    public void shouldInsertDuplicatedNamedButDifferentTypedWallet() {
        Wallet first = getSimpleWallet(Wallet.Type.MY_WALLET);
        List<Wallet> all = walletDataManager.getAll();
        walletDataManager.insert(getSimpleWallet(Wallet.Type.MY_WALLET));
        walletDataManager.insert(getSimpleWallet(Wallet.Type.CONTRACTOR).setName(first.getName()));

        assertThat(walletDataManager.getMyWallets()).hasSize(1);
        assertThat(walletDataManager.getContractors()).hasSize(1);
    }

    private Wallet getSimpleWallet(Wallet.Type type) {
        return new Wallet().setType(type).setName("Simple wallet-" + System.currentTimeMillis()).setInitialAmount(11.1).setCurrentAmount(11.1);
    }
}
