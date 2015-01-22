package info.korzeniowski.walletplus.test.ui.wallet.list;

import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.SwipeListView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;

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
import info.korzeniowski.walletplus.ui.mywallets.details.MyWalletDetailsFragment;
import info.korzeniowski.walletplus.ui.mywallets.list.MyWalletListFragment;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MyWalletListFragmentTest {

    @InjectView(R.id.swipe_list)
    SwipeListView list;

    @Inject
    @Named("local")
    WalletService mockWalletService;

    private MyWalletListFragment fragment;
    private ActionBarActivity activity;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).addModules(new MockDatabaseModule());
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).inject(this);

        activity = Robolectric.buildActivity(MainActivity.class).create().start().resume().get();
        ListView menuList = (ListView) activity.findViewById(R.id.drawerList);
        Robolectric.shadowOf(menuList).performItemClick(3);

        fragment = (MyWalletListFragment) activity.getSupportFragmentManager().findFragmentByTag(MyWalletListFragment.TAG);

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
        MyWalletDetailsFragment detailsFragment = (MyWalletDetailsFragment) activity.getSupportFragmentManager().findFragmentByTag(MyWalletDetailsFragment.TAG);
        assertThat(detailsFragment).isNotNull();
        assertThat((EditText) detailsFragment.getView().findViewById(R.id.walletName)).hasTextString("");
        assertThat((EditText) detailsFragment.getView().findViewById(R.id.walletInitialAmount)).hasTextString("");
    }

    @Test
    public void shouldItemPositionMatch() {
        int itemPosition = 1;

        assertThat(((Wallet) list.getAdapter().getItem(itemPosition)).getName())
                .isEqualTo(mockWalletService.getMyWallets().get(itemPosition).getName());
    }
}