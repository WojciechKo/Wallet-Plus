package info.korzeniowski.walletplus.test.service.cashflow;

import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
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
    public void shouldChangeWalletAmountAfterInsert() {
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        walletService.insert(myWallet);

        Double currentAmount = myWallet.getCurrentAmount();
        Double amount = 152.3;
        CashFlow cashFlow = new CashFlow()
                .setAmount(amount)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .setDateTime(new Date());

        cashFlowService.insert(cashFlow);

        assertThat(walletService.findById(myWallet.getId()).getCurrentAmount()).isEqualTo(currentAmount + amount);

        cashFlowService.insert(cashFlow.setId(null).setType(CashFlow.Type.EXPANSE));

        assertThat(walletService.findById(myWallet.getId()).getCurrentAmount()).isEqualTo(currentAmount);
    }

    @Test
    public void shouldInsertMissingCategories() {
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        String tag1 = "Tag 1";

        String tag2 = "Tag 2";
        Category category2 = new Category(tag2);

        String tag3 = "Tag 3";

        walletService.insert(myWallet);
        categoryService.insert(new Category(tag1));
        categoryService.insert(category2);
        assertThat(categoryService.count()).isEqualTo(2);

        CashFlow firstCashFlow = new CashFlow()
                .setAmount(200.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .addCategory(Lists.newArrayList(new Category(tag1), category2))
                .setDateTime(new Date());

        cashFlowService.insert(firstCashFlow);

        assertThat(categoryService.count()).isEqualTo(2);
        assertThat(cashFlowService.findById(firstCashFlow.getId()).getCategories()).hasSize(2);

        CashFlow secondCashFlow = new CashFlow()
                .setAmount(300.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.EXPANSE)
                .addCategory(Lists.newArrayList(new Category(tag1), category2, new Category(tag3)))
                .setDateTime(new Date());

        cashFlowService.insert(secondCashFlow);

        assertThat(categoryService.count()).isEqualTo(3);
        assertThat(cashFlowService.findById(firstCashFlow.getId()).getCategories()).hasSize(2);
        assertThat(cashFlowService.findById(secondCashFlow.getId()).getCategories()).hasSize(3);
    }

    @Test
    public void shouldUpdateCashFlowTags() {
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        String tag1 = "Tag 1";

        String tag2 = "Tag 2";
        Category category2 = new Category(tag2);

        String tag3 = "Tag 3";

        walletService.insert(myWallet);
        categoryService.insert(new Category(tag1));
        categoryService.insert(category2);

        CashFlow cashFlow = new CashFlow()
                .setAmount(200.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .addCategory(Lists.newArrayList(new Category(tag1), category2))
                .setDateTime(new Date());

        CashFlow found;
        cashFlowService.insert(cashFlow);
        found = cashFlowService.findById(cashFlow.getId());

        // After insert
        assertThat(categoryService.count()).isEqualTo(2);
        assertThat(found.getCategories()).hasSize(2);

        found.removeCategory(new Category(tag1));
        cashFlowService.update(found);
        found = cashFlowService.findById(cashFlow.getId());

        // After first update
        assertThat(categoryService.count()).isEqualTo(2);
        assertThat(found.getCategories()).hasSize(1);

        found.clearCategories().addCategory(Lists.newArrayList(new Category(tag2), new Category(tag3)));
        cashFlowService.update(found);

        // After second update
        assertThat(categoryService.count()).isEqualTo(3);
        assertThat(found.getCategories()).hasSize(2);
    }
}
