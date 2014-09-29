package info.korzeniowski.walletplus.test.service.cashflow;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.assertj.core.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCashFlowServiceTest {

    @Inject
    @Named("local")
    CashFlowService cashFlowService;

    @Inject
    @Named("local")
    WalletService walletService;

    @Inject
    @Named("local")
    CategoryService categoryService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldFixRelatedWalletsAfterInsertOfCashFlow() {
        float amount = (float) 53.1;
        Wallet from = new Wallet().setName("from").setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0);
        Wallet to = new Wallet().setName("to").setType(Wallet.Type.CONTRACTOR).setInitialAmount(0.0);
        walletService.insert(from);
        walletService.insert(to);
        double fromCurrentAmount = walletService.findById(from.getId()).getCurrentAmount();
        double toCurrentAmount = walletService.findById(to.getId()).getCurrentAmount();
        categoryService.insert(new Category().setType(Category.Type.OTHER).setName("Other"));

        CashFlow cashFlow = new CashFlow.Builder()
                .setAmount(amount)
                .setDateTime(new Date())
                .setFromWallet(from)
                .setToWallet(to)
                .setCategory(cashFlowService.getOtherCategory())
                .build();
        cashFlowService.insert(cashFlow);

        assertThat(walletService.findById(from.getId()).getCurrentAmount()).isEqualTo(fromCurrentAmount - amount);
        assertThat(walletService.findById(to.getId()).getCurrentAmount()).isEqualTo(toCurrentAmount + amount);
    }
}
