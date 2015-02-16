package info.korzeniowski.walletplus.test.ui.wallet.list;

import android.app.Activity;
import android.content.Intent;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.test.module.MockDatabaseModule;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;
import info.korzeniowski.walletplus.ui.wallets.list.WalletListActivity;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MyWalletListTest {

    @InjectView(R.id.swipe_list)
    SwipeListView list;

    @Inject
    @Named("local")
    WalletService mockWalletService;

    private Activity activity;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).removeModule(TestDatabaseModule.class);
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).addModules(new MockDatabaseModule());
        ((TestWalletPlus) Robolectric.application.getApplicationContext()).inject(this);

        Mockito.when(mockWalletService.getAll()).thenReturn(Lists.newArrayList(new Wallet().setId(1L).setName("Wallet 1"), new Wallet().setId(2L).setName("Wallet 2")));

        activity = Robolectric.buildActivity(WalletListActivity.class).create().start().resume().get();
        ButterKnife.inject(this, activity);
    }

    @Test
    public void shouldOpenFragmentToCreateNewWallet() {
        activity.onOptionsItemSelected(new TestMenuItem(R.id.menu_new));

        Intent expectedIntent = new Intent(activity, WalletDetailsActivity.class);
        assertThat(Robolectric.shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);
    }

    @Test
    public void shouldItemPositionMatch() {
        int testItemPosition = 1;
        assertThat(((Wallet) list.getAdapter().getItem(testItemPosition)).getName())
                .isEqualTo(mockWalletService.getAll().get(testItemPosition).getName());
    }
}