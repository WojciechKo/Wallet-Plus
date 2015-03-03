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
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.TestDatabaseModule;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CashFlowServiceOrmLiteTest {

    @Inject
    CashFlowService cashFlowService;

    @Inject
    WalletService walletService;

    @Inject
    TagService tagService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).addModules(new TestDatabaseModule(Robolectric.application));
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldIncreaseWalletCurrentAmountAfterInsertIncomeCashFlow() {
        // given
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        walletService.insert(myWallet);

        Double currentAmount = myWallet.getCurrentAmount();
        Double amount = 152.3;
        CashFlow cashFlow = new CashFlow()
                .setAmount(amount)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .setDateTime(new Date());

        // when
        cashFlowService.insert(cashFlow);

        // then
        assertThat(walletService.findById(myWallet.getId()).getCurrentAmount()).isEqualTo(currentAmount + amount);
    }

    @Test
    public void shouldDecreaseWalletCurrentAmountAfterInsertExpenseCashFlow() {
        // given
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        walletService.insert(myWallet);

        Double currentAmount = myWallet.getCurrentAmount();
        Double amount = 152.3;
        CashFlow cashFlow = new CashFlow()
                .setAmount(amount)
                .setWallet(myWallet)
                .setType(CashFlow.Type.EXPANSE)
                .setDateTime(new Date());

        // when
        cashFlowService.insert(cashFlow);

        // then
        assertThat(walletService.findById(myWallet.getId()).getCurrentAmount()).isEqualTo(currentAmount - amount);
    }

    @Test
    public void shouldCreateMissingTagsFromNewCashFlow() {
        // given
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        walletService.insert(myWallet);

        String tag1 = "Tag 1";
        tagService.insert(new Tag(tag1));

        String tag2 = "Tag 2";
        CashFlow cashFlow = new CashFlow()
                .setAmount(200.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .addTag(Lists.newArrayList(new Tag(tag1), new Tag(tag2)))
                .setDateTime(new Date());

        // when
        cashFlowService.insert(cashFlow);

        // then
        Tag foundTag1 = tagService.findByName(tag1);
        assertThat(foundTag1.getName()).isEqualTo(tag1);

        Tag foundTag2 = tagService.findByName(tag2);
        assertThat(foundTag2.getName()).isEqualTo(tag2);

        List<Tag> tagsOfCashFlow = cashFlowService.findById(cashFlow.getId()).getTags();
        assertThat(tagsOfCashFlow).containsOnly(foundTag1, foundTag2);
    }

    @Test
    public void shouldUpdateCashFlowTags() {
        // given
        Wallet myWallet = new Wallet().setName("My wallet").setInitialAmount(100.0);
        walletService.insert(myWallet);

        Tag tag1 = new Tag("Tag 1");
        tagService.insert(tag1);
        Tag tag2 = new Tag("Tag 2");
        tagService.insert(tag2);
        Tag tag3 = new Tag("Tag 3");
        tagService.insert(tag3);

        CashFlow cashFlow = new CashFlow()
                .setAmount(200.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .addTag(Lists.newArrayList(tag1, tag2))
                .setDateTime(new Date());
        cashFlowService.insert(cashFlow);

        CashFlow found = cashFlowService.findById(cashFlow.getId());

        // when
        cashFlowService.update(found.clearTags().addTag(tag2, tag3));

        // then
        found = cashFlowService.findById(cashFlow.getId());
        assertThat(found.getTags()).containsOnly(tag3, tag2);
        assertThat(tagService.getAll()).containsOnly(tag1, tag2, tag3);
    }
}
