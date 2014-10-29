package info.korzeniowski.walletplus.test.ui.wallet.list;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.apache.maven.artifact.ant.shaded.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;
import org.robolectric.util.ActivityController;
import org.robolectric.util.FragmentTestUtil;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.test.module.MockDatabaseModule;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;
import info.korzeniowski.walletplus.ui.wallet.details.WalletDetailsFragment;
import info.korzeniowski.walletplus.ui.wallet.list.WalletListFragment;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;

@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WalletListFragmentTest {

    @InjectView(R.id.list)
    ListView list;

    @Inject
    @Named("local")
    WalletService mockWalletService;

    private WalletListFragment fragment;
    private MockDatabaseModule module;
    private ActionBarActivity activity;

    @Before
    public void setUp() {
        module = new MockDatabaseModule();
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).removeModule(TestDatabaseModule.class);
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).addModules(module);
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).inject(this);

        activity = Robolectric.buildActivity(MainActivity.class).create().start().resume().get();
        ListView menuList = (ListView) activity.findViewById(R.id.drawer);
        Robolectric.shadowOf(menuList).performItemClick(3);

        fragment = (WalletListFragment) activity.getSupportFragmentManager().findFragmentByTag(WalletListFragment.TAG);

        ButterKnife.inject(this, fragment.getView());
    }

//    TODO: change it to check textValue in dialog.
//    @Test
//    public void testConfirmationMessage() {
//        assertThat(MessageFormat.format(message, 0)).isEqualTo("Do you want to delete this wallet?");
//        assertThat(MessageFormat.format(message, 1)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 1 cashflow.");
//        assertThat(MessageFormat.format(message, 2)).isEqualTo("Do you want to delete this wallet?\\n\\nYou will also delete 2 cashflows.");
//    }

    @Test
    public void shouldOpenFragmentToCreateNewWallet() {
        fragment.onOptionsItemSelected(new TestMenuItem(R.id.menu_new));
        WalletDetailsFragment detailsFragment = (WalletDetailsFragment) activity.getSupportFragmentManager().findFragmentByTag(WalletDetailsFragment.TAG);
        assertThat(detailsFragment).isNotNull();
        assertThat((EditText) detailsFragment.getView().findViewById(R.id.walletName)).hasTextString("");
        assertThat((EditText) detailsFragment.getView().findViewById(R.id.walletInitialAmount)).hasTextString("");
    }

    @Test
    public void shouldOpenFragmentToEditWallet() {
        int position = 2;
        Wallet wallet = (Wallet) list.getAdapter().getItem(position);
        Robolectric.shadowOf(list).performItemClick(position);

        WalletDetailsFragment detailsFragment = (WalletDetailsFragment) activity.getSupportFragmentManager().findFragmentByTag(WalletDetailsFragment.TAG);
        assertThat(detailsFragment).isNotNull();
        assertThat((EditText) detailsFragment.getView().findViewById(R.id.walletName)).hasTextString(wallet.getName());
        assertThat(Double.parseDouble(((EditText) detailsFragment.getView().findViewById(R.id.walletInitialAmount)).getText().toString()))
                .isEqualTo(wallet.getInitialAmount());
        assertThat((TextView) detailsFragment.getView().findViewById(R.id.walletCurrentAmount)).isNotNull();
    }

    @Test
    public void shouldItemPositionMatch() {
        int itemPosition = 1;

        assertThat(((Wallet) list.getAdapter().getItem(itemPosition)).getName())
                .isEqualTo(mockWalletService.getMyWallets().get(itemPosition).getName());
    }
}