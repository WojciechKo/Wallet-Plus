package info.korzeniowski.walletplus.test.service.cashflow;

import org.joda.time.DateTime;
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

import static org.fest.assertions.api.Assertions.assertThat;

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
        Double amount = 53.1;
        Wallet from = new Wallet().setName("From").setInitialAmount(100.0);
        walletService.insert(from);
        double fromCurrentAmount = walletService.findById(from.getId()).getCurrentAmount();
        categoryService.insert(new Category().setName("Other"));

        CashFlow cashFlow = new CashFlow()
                .setAmount(amount)
                .setDateTime(new Date())
                .setWallet(from);
        cashFlowService.insert(cashFlow);

        assertThat(walletService.findById(from.getId()).getCurrentAmount()).isEqualTo(fromCurrentAmount - amount);
    }

    @Test
    public void shouldFilterCashFlows() {
        Wallet myWallet = new Wallet().setName("from").setInitialAmount(100.0);
        Category category = new Category().setName("Category");

        walletService.insert(myWallet);
        categoryService.insert(new Category().setName("Other"));
        categoryService.insert(category);

        DateTime now = DateTime.now();
        cashFlowService.insert(new CashFlow().setDateTime(now.minusDays(1).toDate()).setAmount(50.0).setCategory(category).setWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.minusDays(1).toDate()).setAmount(10.0).setToWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.toDate()).setAmount(15.0).setWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.toDate()).setAmount(80.0).setCategory(category).setToWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.toDate()).setAmount(94.0).setToWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.plusDays(1).toDate()).setAmount(500.0).setToWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.plusDays(1).toDate()).setAmount(600.0).setCategory(category).setWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.plusDays(1).toDate()).setAmount(900.0).setCategory(category).setToWallet(myWallet));
        cashFlowService.insert(new CashFlow().setDateTime(now.plusDays(1).toDate()).setAmount(800.0).setToWallet(myWallet));


        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(2).toDate(), category.getId(), null, null)).hasSize(4);
        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(2).toDate(), (Long) null, myWallet.getId(), null)).hasSize(3);
        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(2).toDate(), (Long) null, null, myWallet.getId())).hasSize(6);
        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(2).toDate(), category.getId(), null, myWallet.getId())).hasSize(2);

        assertThat(cashFlowService.findCashFlow(now.toDate(), now.plusDays(2).toDate(), category.getId(),null, null)).hasSize(3);
        assertThat(cashFlowService.findCashFlow(now.toDate(), now.plusDays(2).toDate(), (Long) null, myWallet.getId(), null)).hasSize(2);
        assertThat(cashFlowService.findCashFlow(now.toDate(), now.plusDays(2).toDate(), (Long) null, null, myWallet.getId())).hasSize(5);
        assertThat(cashFlowService.findCashFlow(now.toDate(), now.plusDays(2).toDate(), category.getId(), null, myWallet.getId())).hasSize(2);

        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(1).toDate(), category.getId(), null, null)).hasSize(2);
        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(1).toDate(), (Long) null, myWallet.getId(), null)).hasSize(2);
        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(1).toDate(), (Long) null, null, myWallet.getId())).hasSize(3);
        assertThat(cashFlowService.findCashFlow(now.minusDays(1).toDate(), now.plusDays(1).toDate(), category.getId(), null, myWallet.getId())).hasSize(1);
    }
}
