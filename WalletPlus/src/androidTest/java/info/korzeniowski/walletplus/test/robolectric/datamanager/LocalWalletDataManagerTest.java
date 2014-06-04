package info.korzeniowski.walletplus.test.robolectric.datamanager;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.exception.EntityPropertyCannotBeNullException;
import info.korzeniowski.walletplus.datamanager.exception.WalletHaveToHaveTypeException;
import info.korzeniowski.walletplus.datamanager.exception.WalletNameAndTypeMustBeUniqueException;
import info.korzeniowski.walletplus.datamanager.exception.WalletTypeCannotBeChangedException;
import info.korzeniowski.walletplus.datamanager.local.LocalWalletDataManager;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalWalletDataManagerTest {
    WalletDataManager walletDataManager;

    @Before
    public void setUp() {
        SQLiteOpenHelper dbHelper = new DaoMaster.DevOpenHelper(Robolectric.application, null, null);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        walletDataManager = new LocalWalletDataManager(daoSession.getGreenWalletDao());
    }

    @Test
    public void changeWalletTypeShouldThrowException() {
        Long id = walletDataManager.insert(new Wallet().setName("Name").setType(Wallet.Type.MY_WALLET).setInitialAmount(11.1));
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
        Wallet myWallet = new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(11.1);
        Wallet contractor = new Wallet().setType(Wallet.Type.CONTRACTOR).setInitialAmount(22.2);
        walletDataManager.insert(myWallet.setId(null).setName("A"));
        walletDataManager.insert(contractor.setId(null).setName("C"));
        walletDataManager.insert(contractor.setId(null).setName("D"));
        walletDataManager.insert(myWallet.setId(null).setName("E"));
        walletDataManager.insert(contractor.setId(null).setName("F"));
        walletDataManager.insert(contractor.setId(null).setName("G"));
        walletDataManager.insert(myWallet.setId(null).setName("H"));

        assertThat(walletDataManager.getContractors()).hasSize(4);
        assertThat(walletDataManager.getMyWallets()).hasSize(3);
        assertThat(walletDataManager.getAll()).hasSize(7);
    }

    @Test
    public void shouldUpdateWalletData() {
        String oldName = "oldName";
        double oldAmount = 11.1;
        String newName = "newName";
        double newAmount = 44.4;
        Long id = walletDataManager.insert(new Wallet().setName(oldName).setInitialAmount(oldAmount).setType(Wallet.Type.MY_WALLET));
        Wallet toUpdate = walletDataManager.findById(id);

        walletDataManager.update(toUpdate.setName(newName).setInitialAmount(newAmount));
        Wallet updated = walletDataManager.findById(id);

        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getInitialAmount()).isEqualTo(newAmount);
    }

    @Test
    public void shouldRemoveWallet() {
        Long myWalletId1 = walletDataManager.insert(new Wallet().setName("A").setType(Wallet.Type.MY_WALLET).setInitialAmount(22.2));
        Long myWalletId2 = walletDataManager.insert(new Wallet().setName("B").setType(Wallet.Type.MY_WALLET).setInitialAmount(33.3));
        Long contractorId = walletDataManager.insert(new Wallet().setName("C").setType(Wallet.Type.CONTRACTOR).setInitialAmount(44.4));

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
        walletDataManager.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setName("MyWallet").setInitialAmount(11.1));
        try {
            walletDataManager.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setName("MyWallet").setInitialAmount(11.1));
            failBecauseExceptionWasNotThrown(WalletNameAndTypeMustBeUniqueException.class);
        } catch (WalletNameAndTypeMustBeUniqueException e) {
            assertThat(walletDataManager.getMyWallets()).hasSize(1);
        }
    }

    @Test
    public void shouldInsertDuplicatedNamedButDifferentTypedWallet() {
        walletDataManager.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setName("MyWallet").setInitialAmount(11.1));

        walletDataManager.insert(new Wallet().setType(Wallet.Type.CONTRACTOR).setName("MyWallet").setInitialAmount(11.1));

        assertThat(walletDataManager.getMyWallets()).hasSize(1);
        assertThat(walletDataManager.getContractors()).hasSize(1);
    }

    @Test
    public void walletShouldHaveType() {
        try {
            walletDataManager.insert(new Wallet().setName("TestName").setInitialAmount(11.1));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeNullException.class);
        } catch (EntityPropertyCannotBeNullException e) {
            assertThat(e.getProperty().equals("Type"));
        }
    }

    @Test
    public void walletShouldHaveInitialAmount() {
        try {
            walletDataManager.insert(new Wallet().setName("TestName").setInitialAmount(11.1));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeNullException.class);
        } catch (EntityPropertyCannotBeNullException e) {
            assertThat(e.getProperty().equals("InitialAmount"));
        }
    }
}
