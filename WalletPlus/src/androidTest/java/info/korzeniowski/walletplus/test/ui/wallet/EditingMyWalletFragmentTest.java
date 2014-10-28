package info.korzeniowski.walletplus.test.ui.wallet;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.test.module.MockDatabaseModule;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;
import info.korzeniowski.walletplus.ui.wallet.details.WalletDetailsFragment;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;


@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class EditingMyWalletFragmentTest {

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

    @Inject
    @Named("local")
    CashFlowService mockCashFlowService;

    private Wallet wallet;
    private MockDatabaseModule module;
    private WalletDetailsFragment fragment;

    @Before
    public void setUp() {
        Long walletId = 47L;
        module = new MockDatabaseModule();
        ((TestWalletPlus) Robolectric.application).removeModule(TestDatabaseModule.class);
        ((TestWalletPlus) Robolectric.application).addModules(module);
        ((TestWalletPlus) Robolectric.application).inject(this);

        fragment = new WalletDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(WalletDetailsFragment.WALLET_ID, walletId);
        fragment.setArguments(arguments);

        wallet = new Wallet()
                .setId(walletId)
                .setType(Wallet.Type.MY_WALLET)
                .setName("walletName")
                .setInitialAmount(1600.0)
                .setCurrentAmount(500.0);
        Mockito.when(mockWalletService.findById(walletId)).thenReturn(wallet);

        TextView test = new TextView(Robolectric.application);
        test.setText(mockWalletService.findById(walletId).getName());
        assertThat(test).containsText(wallet.getName());

        FragmentTestUtil.startFragment(fragment);
        ActivityController.of(fragment.getActivity()).visible();
        ButterKnife.inject(this, fragment.getView());
    }

    @Test
    public void shouldCurrentAmountBeVisible() {
        assertThat(walletCurrentAmountLabel).isVisible();
        assertThat(walletCurrentAmount).isVisible();
    }

    @Test
    public void shouldShowsNameLabelAndValidateInput() {
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).isVisible();
        assertThat(walletName).hasNoError();
        assertThat(walletName).hasTextString(wallet.getName());

        walletName.setText("");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasError(R.string.walletNameIsRequired);

        walletName.setText("text");
        assertThat(walletNameLabel).isVisible();
        assertThat(walletName).hasNoError();
    }

    @Test
    public void shouldInitialAmountLabelAppearsAfterTextInput() {
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).isVisible();
        assertThat(walletInitialAmount).hasNoError();

        String previousCurrentAmount = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("0");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();
        assertThat(walletCurrentAmount).doesNotContainText(previousCurrentAmount);

        String currentAmountWhileInitialIsZero = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("100.0");
        assertThat(walletCurrentAmount).doesNotContainText(currentAmountWhileInitialIsZero);

        walletInitialAmount.setText("");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsRequired);
        assertThat(walletCurrentAmount).hasTextString(currentAmountWhileInitialIsZero);

        previousCurrentAmount = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("xxx");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsNotADigit);
        assertThat(walletCurrentAmount).hasTextString(previousCurrentAmount);

        previousCurrentAmount = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasError(R.string.walletInitialAmountIsRequired);
        assertThat(walletCurrentAmount).hasTextString(currentAmountWhileInitialIsZero);

        previousCurrentAmount = walletCurrentAmount.getText().toString();

        walletInitialAmount.setText("12.2");
        assertThat(walletInitialAmountLabel).isVisible();
        assertThat(walletInitialAmount).hasNoError();
        assertThat(walletCurrentAmount).doesNotContainText(previousCurrentAmount);
    }

    @Test
    public void shouldCurrentAmountChangeAfterChangeInitialAmount() {
        String currentAmountString = walletCurrentAmount.getText().toString().replaceAll("[^\\d.]+", "");
        Double currentAmount = Double.parseDouble(currentAmountString);
        int difference = 200;

        Double newInitialAmount = wallet.getInitialAmount() + difference;
        walletInitialAmount.setText(newInitialAmount.toString());

        String newCurrentAmountString = walletCurrentAmount.getText().toString().replaceAll("[^\\d.]+", "");
        Double newCurrentAmount = Double.parseDouble(newCurrentAmountString);
        assertThat(newCurrentAmount).isEqualTo(currentAmount + difference);
    }

    @Test
    public void shouldCallUpdateWallet() {
        walletInitialAmount.setText("150.0");
        walletName.setText("newTextName");
        Wallet toUpdate = new Wallet()
                .setId(wallet.getId())
                .setName(walletName.getText().toString())
                .setInitialAmount(Double.parseDouble(walletInitialAmount.getText().toString()))
                .setType(Wallet.Type.MY_WALLET)
                .setCurrentAmount(wallet.getCurrentAmount());

        fragment.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));

        Mockito.verify(mockWalletService, Mockito.times(1)).update(toUpdate);
    }

    @Test
    public void shouldNotCallUpdateWhenErrors() {
        walletName.setError("simple error");
        fragment.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));
        Mockito.verify(mockWalletService, Mockito.never()).update(Mockito.any(Wallet.class));

        walletName.setError(null);
        walletInitialAmount.setError("other error");
        fragment.onOptionsItemSelected(new TestMenuItem(R.id.menu_save));
        Mockito.verify(mockWalletService, Mockito.never()).update(Mockito.any(Wallet.class));
    }
}
