package info.korzeniowski.walletplus.test.ui.wallet.details;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.test.module.MockDatabaseModule;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;
import info.korzeniowski.walletplus.ui.wallet.details.WalletDetailsFragment;
import info.korzeniowski.walletplus.ui.wallet.list.WalletListFragment;

import static org.fest.assertions.api.ANDROID.assertThat;


@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class AddingMyWalletFragmentTest {

    @InjectView(R.id.walletNameLabel)
    TextView walletNameLabel;

    @InjectView(R.id.walletName)
    EditText walletName;

    @InjectView(R.id.walletInitialAmountLabel)
    TextView walletInitialAmountLabel;

    @InjectView(R.id.walletInitialAmount)
    EditText walletInitialAmount;

    @InjectView(R.id.walletCurrentAmountLabel)
    TextView walletCurrentAmountLabel;

    @InjectView(R.id.walletCurrentAmount)
    TextView walletCurrentAmount;

    @Inject
    @Named("local")
    WalletService mockWalletService;

    private WalletDetailsFragment fragment;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).removeModule(TestDatabaseModule.class);
        ((TestWalletPlus) Robolectric.application).addModules(new MockDatabaseModule());
        ((TestWalletPlus) Robolectric.application).inject(this);

        ActionBarActivity activity = Robolectric.buildActivity(MainActivity.class).create().start().resume().get();
        ListView menuList = (ListView) activity.findViewById(R.id.drawerList);
        Robolectric.shadowOf(menuList).performItemClick(3);
        Fragment walletList = activity.getSupportFragmentManager().findFragmentByTag(WalletListFragment.TAG);
        walletList.onOptionsItemSelected(new TestMenuItem(R.id.menu_new));
        fragment = (WalletDetailsFragment) activity.getSupportFragmentManager().findFragmentByTag(WalletDetailsFragment.TAG);

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
    public void shouldCurrentAmountBeInvisible() {
        assertThat(walletCurrentAmountLabel).isInvisible();
        assertThat(walletCurrentAmount).isInvisible();

    }

    @Test
    public void shouldNameLabelAppearsAfterTextInput() {
        assertThat(walletNameLabel).isInvisible();
        assertThat(walletName).isVisible();
        assertThat(walletName).hasNoError();

        walletName.setText("test");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasNoError();

        walletName.setText("");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasError(R.string.walletNameIsRequired);

        walletName.setText("text");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasNoError();
    }

    @Test
    public void shouldInitialAmountLabelAppearsAfterTextInput() {
        assertThat(walletInitialAmountLabel).isInvisible();
        assertThat(walletInitialAmount).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        walletInitialAmount.setText("1.21");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        walletInitialAmount.setText("");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsRequired);

        walletInitialAmount.setText("xxx");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsNotADigit);

        walletInitialAmount.setText("");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsRequired);

        walletInitialAmount.setText("12.2");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();
    }


    @Test
    public void shouldCallUpdateWallet() {
        walletInitialAmount.setText("150.0");
        walletName.setText("textName");
        Wallet toInsert = new Wallet()
                .setName(walletName.getText().toString())
                .setInitialAmount(Double.parseDouble(walletInitialAmount.getText().toString()))
                .setType(Wallet.Type.MY_WALLET);

        fragment.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));

        Mockito.verify(mockWalletService, Mockito.times(1)).insert(toInsert);
    }

    @Test
    public void shouldNotCallUpdateWhenErrors() {
        walletName.setError("simple error");
        fragment.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));
        Mockito.verify(mockWalletService, Mockito.never()).insert(Mockito.any(Wallet.class));

        walletName.setError(null);
        walletInitialAmount.setError("other error");
        fragment.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));
        Mockito.verify(mockWalletService, Mockito.never()).insert(Mockito.any(Wallet.class));
    }
}
