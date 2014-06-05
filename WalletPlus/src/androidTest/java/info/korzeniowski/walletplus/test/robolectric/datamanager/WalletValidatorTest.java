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
import info.korzeniowski.walletplus.datamanager.exception.EntityPropertyCannotBeEmptyException;
import info.korzeniowski.walletplus.datamanager.local.LocalWalletDataManager;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletValidatorTest {
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
    public void walletMustHaveType() {
        try {
            walletDataManager.insert(new Wallet().setName("TestName").setInitialAmount(11.1));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeEmptyException.class);
        } catch (EntityPropertyCannotBeEmptyException e) {
            assertThat(e.getProperty().equals("Type"));
        }
    }

    @Test
    public void myWalletShouldHaveInitialAmount() {
        try {
            walletDataManager.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setName("TestName"));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeEmptyException.class);
        } catch (EntityPropertyCannotBeEmptyException e) {
            assertThat(e.getProperty().equals("InitialAmount"));
        }
    }
}
