package info.korzeniowski.walletplus.test.service.cashflow;

import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
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
                .addTag(new Tag("tag-1"))
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
                .addTag(new Tag("tag-1"))
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

        String tag1 = "Tag-1";
        tagService.insert(new Tag(tag1));

        String tag2 = "Tag-2";
        CashFlow cashFlow = new CashFlow()
                .setAmount(200.0)
                .setWallet(myWallet)
                .setType(CashFlow.Type.INCOME)
                .addTag(Lists.newArrayList(new Tag(tag1), new Tag(tag2), new Tag(tag1), new Tag(tag2)))
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

        Tag tag1 = new Tag("Tag-1");
        tagService.insert(tag1);
        Tag tag2 = new Tag("Tag-2");
        tagService.insert(tag2);
        Tag tag3 = new Tag("Tag-3");
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

    @Test
    public void shouldReturnOrderedLastNCashFlows() {
        // given
        Wallet wallet = new Wallet().setName("wallet").setInitialAmount(100.0);
        walletService.insert(wallet);

        DateTime now = DateTime.now();


        CashFlow cashFlow1 = new CashFlow()
                .setAmount(10.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.minutes(), -10).toDate())
                .setType(CashFlow.Type.INCOME)
                .addTag(new Tag("tag-1"))
                .setWallet(wallet);
        cashFlowService.insert(cashFlow1);

        CashFlow cashFlow2 = new CashFlow()
                .setAmount(20.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.minutes(), -8).toDate())
                .setType(CashFlow.Type.EXPANSE)
                .addTag(new Tag("tag-2"))
                .setWallet(wallet);
        cashFlowService.insert(cashFlow2);

        CashFlow cashFlow3 = new CashFlow()
                .setAmount(30.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.minutes(), -6).toDate())
                .setType(CashFlow.Type.INCOME)
                .addTag(new Tag("tag-1"))
                .setWallet(wallet);
        cashFlowService.insert(cashFlow3);

        CashFlow cashFlow4 = new CashFlow()
                .setAmount(40.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.minutes(), -4).toDate())
                .setType(CashFlow.Type.INCOME)
                .addTag(new Tag("tag-2"))
                .setWallet(wallet);
        cashFlowService.insert(cashFlow4);

        // then
        assertThat(cashFlowService.getLastNCashFlows(1)).containsExactly(cashFlow4);
        assertThat(cashFlowService.getLastNCashFlows(2)).containsExactly(cashFlow3, cashFlow4);
        assertThat(cashFlowService.getLastNCashFlows(3)).containsExactly(cashFlow2, cashFlow3, cashFlow4);
        assertThat(cashFlowService.getLastNCashFlows(4)).containsExactly(cashFlow1, cashFlow2, cashFlow3, cashFlow4);
    }

    @Test
    public void shouldFindCashFlowByDate() {
        // given
        Wallet wallet = new Wallet().setName("wallet").setInitialAmount(100.0);
        walletService.insert(wallet);

        DateTime now = DateTime.now();

        CashFlow cashFlow1 = new CashFlow()
                .setAmount(10.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -3).toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow1);

        CashFlow cashFlow2 = new CashFlow()
                .setAmount(20.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -2).toDate())
                .setType(CashFlow.Type.EXPANSE)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow2);

        CashFlow cashFlow3 = new CashFlow()
                .setAmount(30.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -1).toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow3);

        CashFlow cashFlow4 = new CashFlow()
                .setAmount(40.0)
                .setDateTime(now.toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow4);

        // then
        assertThat(cashFlowService.findCashFlows(
                new CashFlowService.CashFlowQuery()
                        .withFromDate(now.withFieldAdded(DurationFieldType.days(), -2).toDate())
                        .withToDate(now.withFieldAdded(DurationFieldType.days(), -1).toDate()))
        ).containsExactly(cashFlow3, cashFlow2);

        assertThat(cashFlowService.findCashFlows(
                        new CashFlowService.CashFlowQuery()
                                .withFromDate(now.withFieldAdded(DurationFieldType.days(), -2).toDate()))
        ).containsExactly(cashFlow4, cashFlow3, cashFlow2);

        assertThat(cashFlowService.findCashFlows(
                        new CashFlowService.CashFlowQuery()
                                .withToDate(now.withFieldAdded(DurationFieldType.days(), -1).toDate()))
        ).containsExactly(cashFlow3, cashFlow2, cashFlow1);

        assertThat(cashFlowService.findCashFlows(
                        new CashFlowService.CashFlowQuery()
                                .withFromDate(now.withFieldAdded(DurationFieldType.days(), -2).withFieldAdded(DurationFieldType.seconds(), 1).toDate())
                                .withToDate(now.withFieldAdded(DurationFieldType.days(), -1).toDate()))
        ).containsExactly(cashFlow3);

        assertThat(cashFlowService.findCashFlows(
                        new CashFlowService.CashFlowQuery()
                                .withFromDate(now.withFieldAdded(DurationFieldType.days(), -1).withFieldAdded(DurationFieldType.seconds(), 1).toDate())
                                .withToDate(now.withFieldAdded(DurationFieldType.days(), -1).withFieldAdded(DurationFieldType.seconds(), -1).toDate()))
        ).isEmpty();
    }

    @Test
    public void shouldFindCashFlowByWallet() {
        // given
        Wallet wallet1 = new Wallet().setName("wallet 1").setInitialAmount(100.0);
        walletService.insert(wallet1);
        Wallet wallet2 = new Wallet().setName("wallet 2").setInitialAmount(100.0);
        walletService.insert(wallet2);

        DateTime now = DateTime.now();

        CashFlow cashFlow1 = new CashFlow()
                .setAmount(10.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -3).toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet1);
        cashFlowService.insert(cashFlow1);

        CashFlow cashFlow2 = new CashFlow()
                .setAmount(20.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -2).toDate())
                .setType(CashFlow.Type.EXPANSE)
                .setWallet(wallet2);
        cashFlowService.insert(cashFlow2);

        CashFlow cashFlow3 = new CashFlow()
                .setAmount(30.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -1).toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet1);
        cashFlowService.insert(cashFlow3);

        CashFlow cashFlow4 = new CashFlow()
                .setAmount(40.0)
                .setDateTime(now.toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet2);
        cashFlowService.insert(cashFlow4);

        // then
        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withWalletId(wallet1.getId())))
                .containsExactly(cashFlow3, cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withWalletId(wallet2.getId())))
                .containsExactly(cashFlow4, cashFlow2);

        assertThat(cashFlowService.findCashFlows(
                        new CashFlowService.CashFlowQuery()
                                .withWalletId(wallet1.getId())
                                .withFromDate(now.withFieldAdded(DurationFieldType.days(), -2).toDate())))
                .containsExactly(cashFlow3);

        assertThat(cashFlowService.findCashFlows(
                        new CashFlowService.CashFlowQuery()
                                .withWalletId(wallet2.getId())
                                .withFromDate(now.withFieldAdded(DurationFieldType.days(), -1).toDate())
                                .withToDate(now.toDate())))
                .containsExactly(cashFlow4);
    }

    @Test
    public void shouldFindCashFlowByAmount() {
        // given
        Wallet wallet = new Wallet().setName("wallet").setInitialAmount(100.0);
        walletService.insert(wallet);

        DateTime now = DateTime.now();

        CashFlow cashFlow1 = new CashFlow()
                .setAmount(10.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -1).toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow1);

        CashFlow cashFlow2 = new CashFlow()
                .setAmount(20.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.hours(), -12).toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow2);

        CashFlow cashFlow3 = new CashFlow()
                .setAmount(30.0)
                .setDateTime(now.toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow3);

        // then
        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withMinAmount(10.0)))
                .containsExactly(cashFlow3, cashFlow2, cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withMinAmount(25.0)))
                .containsExactly(cashFlow3);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withMaxAmount(15.0)))
                .containsExactly(cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withMaxAmount(20.0)))
                .containsExactly(cashFlow2, cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withMinAmount(15.0).withMaxAmount(35.0)))
                .containsExactly(cashFlow3, cashFlow2);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withMaxAmount(20.0).withToDate(now.withFieldAdded(DurationFieldType.hours(), -18).toDate())))
                .containsExactly(cashFlow1);
    }

    @Test
    public void shouldFindCashFlowByTags() {
        // given
        Wallet wallet = new Wallet().setName("wallet").setInitialAmount(100.0);
        walletService.insert(wallet);

        //TODO: Left to check if future tests will raport tag name cann't contain whitespces
        Tag tag1 = new Tag("tag 1");
        tagService.insert(tag1);
        Tag tag2 = new Tag("tag 2");
        tagService.insert(tag2);
        Tag tag3 = new Tag("tag 3");
        tagService.insert(tag3);

        DateTime now = DateTime.now();

        CashFlow cashFlow1 = new CashFlow()
                .setAmount(10.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -9).toDate())
                .addTag(tag1)
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow1);

        CashFlow cashFlow2 = new CashFlow()
                .setAmount(20.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -8).toDate())
                .addTag(tag2)
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow2);

        CashFlow cashFlow3 = new CashFlow()
                .setAmount(30.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -7).toDate())
                .addTag(tag3)
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow3);

        CashFlow cashFlow4 = new CashFlow()
                .setAmount(40.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -6).toDate())
                .addTag(tag1, tag2)
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow4);

        CashFlow cashFlow5 = new CashFlow()
                .setAmount(50.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -5).toDate())
                .addTag(tag1, tag3)
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow5);

        CashFlow cashFlow6 = new CashFlow()
                .setAmount(60.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -4).toDate())
                .addTag(tag2, tag3)
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow6);

        CashFlow cashFlow7 = new CashFlow()
                .setAmount(70.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -3).toDate())
                .addTag(tag1, tag2, tag3)
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow7);

        CashFlow cashFlow8 = new CashFlow()
                .setAmount(80.0)
                .setDateTime(now.withFieldAdded(DurationFieldType.days(), -2).toDate())
                .setType(CashFlow.Type.INCOME)
                .setWallet(wallet);
        cashFlowService.insert(cashFlow8);

        // then
        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withTags(tag1)))
                .containsExactly(cashFlow7, cashFlow5, cashFlow4, cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withTags(tag2)))
                .containsExactly(cashFlow7, cashFlow6, cashFlow4, cashFlow2);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withTags(tag1, tag2)))
                .containsExactly(cashFlow7, cashFlow4);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withoutTags(tag1)))
                .containsExactly(cashFlow8, cashFlow6, cashFlow3, cashFlow2);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withoutTags(tag2)))
                .containsExactly(cashFlow8, cashFlow5, cashFlow3, cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withoutTags(tag1, tag2)))
                .containsExactly(cashFlow8, cashFlow3);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withTags(tag1).withoutTags(tag2)))
                .containsExactly(cashFlow5, cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withTags(tag1).withoutTags(tag2, tag3)))
                .containsExactly(cashFlow1);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withTags(tag1, tag2).withoutTags(tag3)))
                .containsExactly(cashFlow4);

        assertThat(cashFlowService.findCashFlows(new CashFlowService.CashFlowQuery().withTags(tag1).withoutTags(tag1)))
                .isEmpty();
    }
}
