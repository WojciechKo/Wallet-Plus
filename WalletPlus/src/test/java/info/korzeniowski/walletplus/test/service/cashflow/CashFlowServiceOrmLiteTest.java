package info.korzeniowski.walletplus.test.service.cashflow;

import com.google.common.collect.Lists;

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
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CashFlowServiceOrmLiteTest {

    @Inject
    @Named("local")
    CashFlowService cashFlowService;

    @Inject
    @Named("local")
    WalletService walletService;

    @Inject
    @Named("local")
    TagService tagService;

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
        Tag category2 = new Tag(tag2);

        String tag3 = "Tag 3";

        walletService.insert(myWallet);
        tagService.insert(new Tag(tag1));
        tagService.insert(category2);
        assertThat(tagService.count()).isEqualTo(2);

        CashFlow firstCashFlow = new CashFlow()
                .setAmount(200.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .addTag(Lists.newArrayList(new Tag(tag1), category2))
                .setDateTime(new Date());

        cashFlowService.insert(firstCashFlow);

        assertThat(tagService.count()).isEqualTo(2);
        assertThat(cashFlowService.findById(firstCashFlow.getId()).getTags()).hasSize(2);

        CashFlow secondCashFlow = new CashFlow()
                .setAmount(300.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.EXPANSE)
                .addTag(Lists.newArrayList(new Tag(tag1), category2, new Tag(tag3)))
                .setDateTime(new Date());

        cashFlowService.insert(secondCashFlow);

        assertThat(tagService.count()).isEqualTo(3);
        assertThat(cashFlowService.findById(firstCashFlow.getId()).getTags()).hasSize(2);
        assertThat(cashFlowService.findById(secondCashFlow.getId()).getTags()).hasSize(3);
    }

    @Test
    public void shouldUpdateCashFlowTags() {
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        String tag1 = "Tag 1";

        String tag2 = "Tag 2";
        Tag category2 = new Tag(tag2);

        String tag3 = "Tag 3";

        walletService.insert(myWallet);
        tagService.insert(new Tag(tag1));
        tagService.insert(category2);

        CashFlow cashFlow = new CashFlow()
                .setAmount(200.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .addTag(Lists.newArrayList(new Tag(tag1), category2))
                .setDateTime(new Date());

        CashFlow found;
        cashFlowService.insert(cashFlow);
        found = cashFlowService.findById(cashFlow.getId());

        // After insert
        assertThat(tagService.count()).isEqualTo(2);
        assertThat(found.getTags()).hasSize(2);

        found.removeTag(new Tag(tag1));
        cashFlowService.update(found);
        found = cashFlowService.findById(cashFlow.getId());

        // After first update
        assertThat(tagService.count()).isEqualTo(2);
        assertThat(found.getTags()).hasSize(1);

        found.clearTags().addTag(Lists.newArrayList(new Tag(tag2), new Tag(tag3)));
        cashFlowService.update(found);

        // After second update
        assertThat(tagService.count()).isEqualTo(3);
        assertThat(found.getTags()).hasSize(2);
    }
}
