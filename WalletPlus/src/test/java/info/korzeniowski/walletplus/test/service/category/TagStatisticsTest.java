package info.korzeniowski.walletplus.test.service.category;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

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

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldReturnProperCategoryStats() {
        Tag tag = new Tag().setName("Test category");
        tagService.insert(tag);
        walletService.insert(new Wallet().setInitialAmount(100.0).setName("Wallet 1"));
        walletService.insert(new Wallet().setInitialAmount(100.0).setName("Wallet 2"));

        Date today = DateTime.now().toDate();
        Date yesterday = new DateTime(today).minusDays(1).toDate();
        Date tomorrow = new DateTime(today).plusDays(1).toDate();

        // yesterday
        // -5 +7 -1
        cashFlowService.insert(new CashFlow().setWallet(walletService.getMyWallets().get(0)).setAmount(5.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setAmount(7.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setWallet(walletService.getMyWallets().get(0)).setAmount(1.0).setDateTime(yesterday));

        // today
        // +50 +70 -90
        cashFlowService.insert(new CashFlow().setAmount(50.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setAmount(70.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setWallet(walletService.getMyWallets().get(1)).setAmount(90.0).setDateTime(today));

        // tomorrow
        // +100 -300 +700
        cashFlowService.insert(new CashFlow().setAmount(100.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setWallet(walletService.getMyWallets().get(1)).setAmount(300.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setAmount(700.0).setDateTime(tomorrow));

        TagService.TagStats stats;

        stats = tagService.getTagStats(tag, yesterday, Period.days(1), 0);
        assertThat(stats.getIncome()).isEqualTo(7.0);
        assertThat(stats.getExpense()).isEqualTo(6.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, yesterday, Period.days(1), 0));

        stats = tagService.getTagStats(tag, yesterday, Period.days(1), 1);
        assertThat(stats.getIncome()).isEqualTo(120.0);
        assertThat(stats.getExpense()).isEqualTo(90.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, yesterday, Period.days(1), 1));

        stats = tagService.getTagStats(tag, yesterday, Period.days(2), 0);
        assertThat(stats.getIncome()).isEqualTo(127.0);
        assertThat(stats.getExpense()).isEqualTo(96.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, yesterday, Period.days(2), 0));

        stats = tagService.getTagStats(tag, today, Period.days(1), -1);
        assertThat(stats.getIncome()).isEqualTo(7.0);
        assertThat(stats.getExpense()).isEqualTo(6.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, today, Period.days(1), -1));

        stats = tagService.getTagStats(tag, today, Period.days(1), 0);
        assertThat(stats.getIncome()).isEqualTo(120.0);
        assertThat(stats.getExpense()).isEqualTo(90.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, today, Period.days(1), 0));

        stats = tagService.getTagStats(tag, today, Period.days(2), 0);
        assertThat(stats.getIncome()).isEqualTo(920.0);
        assertThat(stats.getExpense()).isEqualTo(390.0);
        assertThat(stats).isEqualTo(getCategoryStatsFromCategoryStateList(tag, today, Period.days(2), 0));
    }

    @Test
    public void shouldReturnProperCategoryStatsForMain() {
        Tag mainTag = new Tag().setName("Main category");
        tagService.insert(mainTag);
        Tag subTag = new Tag().setName("Sub category");
        tagService.insert(subTag);
        Wallet myWallet = new Wallet().setInitialAmount(100.0).setName("Wallet");
        walletService.insert(myWallet);

        Date today = DateTime.now().toDate();

        cashFlowService.insert(new CashFlow().setWallet(walletService.getMyWallets().get(0)).setAmount(3.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setAmount(5.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setWallet(walletService.getMyWallets().get(0)).setAmount(7.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setAmount(11.0).setDateTime(today));

        TagService.TagStats mainTagStats = tagService.getTagStats(mainTag, today, Period.days(1), 0);
        assertThat(mainTagStats.getIncome()).isEqualTo(11.0);
        assertThat(mainTagStats.getExpense()).isEqualTo(3.0);
        assertThat(mainTagStats).isEqualTo(getCategoryStatsFromCategoryStateList(mainTag, today, Period.days(1), 0));

        TagService.TagStats subTagStats = tagService.getTagStats(subTag, today, Period.days(1), 0);
        assertThat(subTagStats.getIncome()).isEqualTo(5.0);
        assertThat(subTagStats.getExpense()).isEqualTo(7.0);
        assertThat(subTagStats).isEqualTo(getCategoryStatsFromCategoryStateList(subTag, today, Period.days(1), 0));
    }

    private TagService.TagStats getCategoryStatsFromCategoryStateList(final Tag tag, Date yesterday, Period period, Integer iteration) {
        List<TagService.TagStats> tagStatsList = tagService.getTagStatsList(yesterday, period, iteration);

        return Iterables.find(tagStatsList, new Predicate<TagService.TagStats>() {
            @Override
            public boolean apply(TagService.TagStats input) {
                return tag.getId().equals(input.getTagId());
            }
        });
    }
}
