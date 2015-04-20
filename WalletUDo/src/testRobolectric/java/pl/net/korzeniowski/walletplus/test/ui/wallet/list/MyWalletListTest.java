package pl.net.korzeniowski.walletplus.test.ui.wallet.list;

import android.app.Activity;
import android.content.Intent;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.fakes.RoboMenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.net.korzeniowski.walletplus.MyRobolectricTestRunner;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.TestWalletPlus;
import com.walletudo.model.Wallet;
import com.walletudo.service.WalletService;
import com.walletudo.ui.wallets.details.WalletDetailsActivity;
import com.walletudo.ui.wallets.list.WalletListActivity;

import static org.fest.assertions.api.Assertions.assertThat;

@Ignore
@RunWith(MyRobolectricTestRunner.class)
public class MyWalletListTest {

    @InjectView(R.id.swipe_list)
    SwipeListView list;

    @Inject
    WalletService walletServiceMock;

    private Activity activity;

    @Before
    public void setUp() {
        ((TestWalletPlus) RuntimeEnvironment.application).setMockComponent();
        ((TestWalletPlus) RuntimeEnvironment.application).component().inject(this);

        Mockito.when(walletServiceMock.getAll()).thenReturn(Lists.newArrayList(new Wallet().setId(1L).setName("Wallet 1"), new Wallet().setId(2L).setName("Wallet 2")));

        activity = Robolectric.buildActivity(WalletListActivity.class).create().start().resume().get();
        ButterKnife.inject(this, activity);
    }

    @Test
    public void shouldOpenFragmentToCreateNewWallet() {
        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_new));

        Intent expectedIntent = new Intent(activity, WalletDetailsActivity.class);
        assertThat(Shadows.shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);
    }

    @Test
    public void shouldItemPositionMatch() {
        int testItemPosition = 1;
        assertThat(((Wallet) list.getAdapter().getItem(testItemPosition)).getName())
                .isEqualTo(walletServiceMock.getAll().get(testItemPosition).getName());
    }
}