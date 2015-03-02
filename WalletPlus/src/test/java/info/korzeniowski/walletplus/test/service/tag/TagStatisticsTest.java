package info.korzeniowski.walletplus.test.service.tag;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TagStatisticsTest {

    @Inject
    @Named("local")
    TagService tagService;

    @Inject
    @Named("local")
    CashFlowService cashFlowService;

    @Inject
    @Named("local")
    WalletService walletService;

    @Inject
    @Named("local")
    StatisticService statisticService;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldReturnCashFlowsAssociatedToTag() {
        Tag tag1 = new Tag().setName("Tag1");
        tagService.insert(tag1);
        Tag tag2 = new Tag().setName("Tag2");
        tagService.insert(tag2);

        Wallet wallet = new Wallet().setInitialAmount(100.0).setName("Wallet");
        walletService.insert(wallet);

        DateTime temp = DateTime.now();

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.INCOME).setAmount(3.0).addTag(tag1).setWallet(wallet).setDateTime(temp.toDate()));
        temp = temp.plusMinutes(1);

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(5.0).setWallet(wallet).setDateTime(temp.toDate()));
        temp = temp.plusMinutes(1);

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.INCOME).setAmount(7.0).addTag(tag1).addTag(tag2).setWallet(wallet).setDateTime(temp.toDate()));
        temp = temp.plusMinutes(1);

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(11.0).addTag(tag2).setWallet(wallet).setDateTime(temp.toDate()));
        temp = temp.plusMinutes(1);

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.INCOME).setAmount(13.0).addTag(tag1).setWallet(wallet).setDateTime(temp.toDate()));
        temp = temp.plusMinutes(1);

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(17.0).addTag(tag1).setWallet(wallet).setDateTime(temp.toDate()));
        temp = temp.plusMinutes(1);

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.INCOME).setAmount(19.0).addTag(tag1).addTag(tag2).setWallet(wallet).setDateTime(temp.toDate()));
        temp = temp.plusMinutes(1);

        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(23.0).addTag(tag1).setWallet(wallet).setDateTime(temp.toDate()));

        List<CashFlow> result = tagService.getAssociatedCashFlows(tag1.getId(), 3L);
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAmount()).isEqualTo(17.0);
        assertThat(result.get(1).getAmount()).isEqualTo(19.0);
        assertThat(result.get(2).getAmount()).isEqualTo(23.0);
    }

//    @Test
//    public void shouldReturnProperTagStats() {
//        Tag tag = new Tag().setName("Test category");
//        tagService.insert(tag);
//        walletService.insert(new Wallet().setInitialAmount(100.0).setName("Wallet 1"));
//        walletService.insert(new Wallet().setInitialAmount(100.0).setName("Wallet 2"));
//
//        Date today = DateTime.now().toDate();
//        Date yesterday = new DateTime(today).minusDays(1).toDate();
//        Date tomorrow = new DateTime(today).plusDays(1).toDate();
//
//        // yesterday
//        // -5 +7 -1
//        Wallet wallet1 = walletService.getAll().get(0);
//        Wallet wallet2 = walletService.getAll().get(1);
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(5.0).setWallet(wallet1).setDateTime(yesterday));
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.INCOME).setAmount(7.0).setWallet(wallet1).setDateTime(yesterday));
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(1.0).setWallet(wallet2).setDateTime(yesterday));
//
//        // today
//        // +50 +70 -90
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.INCOME).setAmount(50.0).setWallet(wallet2).setDateTime(today));
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.INCOME).setAmount(70.0).setWallet(wallet1).setDateTime(today));
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(90.0).setWallet(wallet2).setDateTime(today));
//
//        // tomorrow
//        // +100 -300 +700
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(100.0).setWallet(wallet1).setDateTime(tomorrow));
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(300.0).setWallet(wallet2).setDateTime(tomorrow));
//        cashFlowService.insert(new CashFlow().setType(CashFlow.Type.EXPANSE).setAmount(700.0).setWallet(wallet1).setDateTime(tomorrow));
//
//        StatisticsService.TagStats stats;
//
//        stats = statisticsService.getTagStats(tag, yesterday, Period.days(1), 0);
//        assertThat(stats.getIncome()).isEqualTo(7.0);
//        assertThat(stats.getExpense()).isEqualTo(6.0);
//        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, yesterday, Period.days(1), 0));
//
//        stats = statisticsService.getTagStats(tag, yesterday, Period.days(1), 1);
//        assertThat(stats.getIncome()).isEqualTo(120.0);
//        assertThat(stats.getExpense()).isEqualTo(90.0);
//        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, yesterday, Period.days(1), 1));
//
//        stats = statisticsService.getTagStats(tag, yesterday, Period.days(2), 0);
//        assertThat(stats.getIncome()).isEqualTo(127.0);
//        assertThat(stats.getExpense()).isEqualTo(96.0);
//        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, yesterday, Period.days(2), 0));
//
//        stats = statisticsService.getTagStats(tag, today, Period.days(1), -1);
//        assertThat(stats.getIncome()).isEqualTo(7.0);
//        assertThat(stats.getExpense()).isEqualTo(6.0);
//        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, today, Period.days(1), -1));
//
//        stats = statisticsService.getTagStats(tag, today, Period.days(1), 0);
//        assertThat(stats.getIncome()).isEqualTo(120.0);
//        assertThat(stats.getExpense()).isEqualTo(90.0);
//        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, today, Period.days(1), 0));
//
//        stats = statisticsService.getTagStats(tag, today, Period.days(2), 0);
//        assertThat(stats.getIncome()).isEqualTo(920.0);
//        assertThat(stats.getExpense()).isEqualTo(390.0);
//        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, today, Period.days(2), 0));
//    }
//
//    @Test
//    public void shouldReturnProperTagStatsForMain() {
//        Tag mainTag = new Tag().setName("Main category");
//        tagService.insert(mainTag);
//        Tag subTag = new Tag().setName("Sub category");
//        tagService.insert(subTag);
//        Wallet myWallet = new Wallet().setInitialAmount(100.0).setName("Wallet");
//        walletService.insert(myWallet);
//
//        Date today = DateTime.now().toDate();
//
//        cashFlowService.insert(new CashFlow().setWallet(walletService.getAll().get(0)).setAmount(3.0).setDateTime(today));
//        cashFlowService.insert(new CashFlow().setAmount(5.0).setDateTime(today));
//        cashFlowService.insert(new CashFlow().setWallet(walletService.getAll().get(0)).setAmount(7.0).setDateTime(today));
//        cashFlowService.insert(new CashFlow().setAmount(11.0).setDateTime(today));
//
//        StatisticsService.TagStats mainTagStats = statisticsService.getTagStats(mainTag, today, Period.days(1), 0);
//        assertThat(mainTagStats.getIncome()).isEqualTo(11.0);
//        assertThat(mainTagStats.getExpense()).isEqualTo(3.0);
//        assertThat(mainTagStats).isEqualTo(getCategoryStatsFromCategoryStateList(mainTag, today, Period.days(1), 0));
//
//        StatisticsService.TagStats subTagStats = statisticsService.getTagStats(subTag, today, Period.days(1), 0);
//        assertThat(subTagStats.getIncome()).isEqualTo(5.0);
//        assertThat(subTagStats.getExpense()).isEqualTo(7.0);
//        assertThat(subTagStats).isEqualTo(getCategoryStatsFromCategoryStateList(subTag, today, Period.days(1), 0));
//    }
//
//    private StatisticsService.TagStats getCategoryStatsFromCategoryStateList(final Tag tag, Date yesterday, Period period, Integer iteration) {
//        List<StatisticsService.TagStats> tagStatsList = statisticsService.getTagStatsList(yesterday, period, iteration);
//
//        return Iterables.find(tagStatsList, new Predicate<StatisticsService.TagStats>() {
//            @Override
//            public boolean apply(StatisticsService.TagStats input) {
//                return tag.getId().equals(input.getTagId());
//            }
//        });
//    }
}
