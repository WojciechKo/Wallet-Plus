package pl.net.korzeniowski.walletplus.test.service.statistic;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

import pl.net.korzeniowski.walletplus.dagger.test.ServiceInjectedUnitTest;
import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.model.Wallet;
import pl.net.korzeniowski.walletplus.service.StatisticService;

import static org.assertj.core.api.Assertions.assertThat;

@SmallTest
public class StatisticServiceOrmLiteTest extends ServiceInjectedUnitTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void shouldCountCashFlowsAssignedToWallet() {
        // given
        Wallet wallet1 = new Wallet().setName("Wallet 1").setInitialAmount(100.0);
        walletService.insert(wallet1);
        Wallet wallet2 = new Wallet().setName("Wallet 2").setInitialAmount(200.0);
        walletService.insert(wallet2);

        CashFlow cashFlow = new CashFlow()
                .setAmount(50.0)
                .setDateTime(new Date());

        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet1).setType(CashFlow.Type.EXPENSE));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet1).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.EXPENSE));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).setWallet(wallet2).setType(CashFlow.Type.INCOME));

        // then
        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet1.getId())).isEqualTo(2);
        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet2.getId())).isEqualTo(3);
    }

    @Test
    public void shouldCountCashFlowsAssignedToTag() {
        // given
        Wallet wallet = new Wallet().setName("Wallet").setInitialAmount(100.0);
        walletService.insert(wallet);
        Tag tag1 = new Tag("tag1");
        tagService.insert(tag1);
        Tag tag2 = new Tag("tag2");
        tagService.insert(tag2);
        Tag tag3 = new Tag("tag3");
        tagService.insert(tag3);

        CashFlow cashFlow = new CashFlow()
                .setAmount(50.0)
                .setWallet(wallet)
                .setDateTime(new Date());

        cashFlowService.insert(cashFlow.setId(null).clearTags().setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1).setType(CashFlow.Type.EXPENSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag2).setType(CashFlow.Type.EXPENSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag3).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1, tag2).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1, tag3).setType(CashFlow.Type.EXPENSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag2, tag3).setType(CashFlow.Type.EXPENSE));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag1, tag2, tag3).setType(CashFlow.Type.INCOME));

        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag2, tag3).setType(CashFlow.Type.INCOME));
        cashFlowService.insert(cashFlow.setId(null).clearTags().addTag(tag3).setType(CashFlow.Type.INCOME));

        // then
        assertThat(statisticService.countCashFlowsAssignedToTag(tag1.getId())).isEqualTo(4);
        assertThat(statisticService.countCashFlowsAssignedToTag(tag2.getId())).isEqualTo(5);
        assertThat(statisticService.countCashFlowsAssignedToTag(tag3.getId())).isEqualTo(6);
    }

    @Test
    public void shouldGetTagStats2ForTag() {
        // given
        Wallet wallet = new Wallet().setName("Wallet").setInitialAmount(100.0);
        walletService.insert(wallet);
        Tag tag1 = new Tag("tag1");
        tagService.insert(tag1);
        Tag tag2 = new Tag("tag2");
        tagService.insert(tag2);
        Tag tag3 = new Tag("tag3");
        tagService.insert(tag3);

        cashFlowService.insert(new CashFlow().addTag(tag1).setAmount(2.0).setType(CashFlow.Type.INCOME).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag2).setAmount(3.0).setType(CashFlow.Type.INCOME).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag3).setAmount(5.0).setType(CashFlow.Type.INCOME).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag1, tag2).setAmount(7.0).setType(CashFlow.Type.INCOME).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag2, tag3).setAmount(11.0).setType(CashFlow.Type.INCOME).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag1, tag3).setAmount(13.0).setType(CashFlow.Type.INCOME).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag1, tag2, tag3).setAmount(17.0).setType(CashFlow.Type.INCOME).setWallet(wallet));

        cashFlowService.insert(new CashFlow().addTag(tag1).setAmount(19.0).setType(CashFlow.Type.EXPENSE).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag2).setAmount(23.0).setType(CashFlow.Type.EXPENSE).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag3).setAmount(29.0).setType(CashFlow.Type.EXPENSE).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag1, tag2).setAmount(31.0).setType(CashFlow.Type.EXPENSE).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag2, tag3).setAmount(37.0).setType(CashFlow.Type.EXPENSE).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag1, tag3).setAmount(41.0).setType(CashFlow.Type.EXPENSE).setWallet(wallet));
        cashFlowService.insert(new CashFlow().addTag(tag1, tag2, tag3).setAmount(43.0).setType(CashFlow.Type.EXPENSE).setWallet(wallet));

        // then

        // For tag1
        Map<Tag, StatisticService.TagStats2> tagStatsForTag1 = statisticService.getTagStats2(tag1);
        assertThat(tagStatsForTag1).hasSize(3);
        assertThat(tagStatsForTag1).containsOnlyKeys(tag1, tag2, tag3);

        assertThat(tagStatsForTag1.get(tag1).getIncome()).isEqualTo(2.0);
        assertThat(tagStatsForTag1.get(tag1).getExpense()).isEqualTo(19.0);
        assertThat(tagStatsForTag1.get(tag2).getIncome()).isEqualTo(24.0);
        assertThat(tagStatsForTag1.get(tag2).getExpense()).isEqualTo(74.0);
        assertThat(tagStatsForTag1.get(tag3).getIncome()).isEqualTo(30.0);
        assertThat(tagStatsForTag1.get(tag3).getExpense()).isEqualTo(84.0);

        // For tag2
        Map<Tag, StatisticService.TagStats2> tagStatsForTag2 = statisticService.getTagStats2(tag2);
        assertThat(tagStatsForTag2.get(tag1).getIncome()).isEqualTo(24.0);
        assertThat(tagStatsForTag2.get(tag1).getExpense()).isEqualTo(74.0);
        assertThat(tagStatsForTag2.get(tag2).getIncome()).isEqualTo(3.0);
        assertThat(tagStatsForTag2.get(tag2).getExpense()).isEqualTo(23.0);
        assertThat(tagStatsForTag2.get(tag3).getIncome()).isEqualTo(28.0);
        assertThat(tagStatsForTag2.get(tag3).getExpense()).isEqualTo(80.0);

        // For tag3
        Map<Tag, StatisticService.TagStats2> tagStatsForTag3 = statisticService.getTagStats2(tag3);
        assertThat(tagStatsForTag3.get(tag1).getIncome()).isEqualTo(30.0);
        assertThat(tagStatsForTag3.get(tag1).getExpense()).isEqualTo(84.0);
        assertThat(tagStatsForTag3.get(tag2).getIncome()).isEqualTo(28.0);
        assertThat(tagStatsForTag3.get(tag2).getExpense()).isEqualTo(80.0);
        assertThat(tagStatsForTag3.get(tag3).getIncome()).isEqualTo(5.0);
        assertThat(tagStatsForTag3.get(tag3).getExpense()).isEqualTo(29.0);
    }
}
