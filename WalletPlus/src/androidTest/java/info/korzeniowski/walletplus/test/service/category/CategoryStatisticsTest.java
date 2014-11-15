package info.korzeniowski.walletplus.test.service.category;

import android.util.Pair;

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
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryStatisticsTest {

    @Inject
    @Named("local")
    CategoryService categoryService;

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
    public void shouldReturnProperStats() {
        Category category = new Category().setType(Category.Type.INCOME_EXPENSE).setName("Test category");
        categoryService.insert(category);
        walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).setName("Wallet 1"));
        walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).setName("Wallet 2"));

        Date today = DateTime.now().toDate();
        Date yesterday = new DateTime(today).minusDays(1).toDate();
        Date tomorrow = new DateTime(today).plusDays(1).toDate();

        // yesterday
        // -5 +7 -1 = +1
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(0)).setAmount(5.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(7.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(0)).setAmount(1.0).setDateTime(yesterday));

        // today
        // +50 +70 -90 = +30
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(0)).setAmount(50.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(70.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(1)).setAmount(90.0).setDateTime(today));

        // tomorrow
        // +100 -300 +700 = 500
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(100.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(1)).setAmount(300.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(700.0).setDateTime(tomorrow));

        CategoryService.CategoryStats stats = null;

        stats = categoryService.getCategoryStats(category, yesterday, Period.days(1), 0);
        assertThat(stats.getDifference()).isEqualTo(1.0);
        assertThat(stats.getFlow()).isEqualTo(13.0);

        stats = categoryService.getCategoryStats(category, yesterday, Period.days(1), 1);
        assertThat(stats.getDifference()).isEqualTo(30.0);
        assertThat(stats.getFlow()).isEqualTo(210.0);

        stats = categoryService.getCategoryStats(category, yesterday, Period.days(2), 0);
        assertThat(stats.getDifference()).isEqualTo(31.0);
        assertThat(stats.getFlow()).isEqualTo(223.0);

        stats = categoryService.getCategoryStats(category, today, Period.days(1), -1);
        assertThat(stats.getDifference()).isEqualTo(1.0);
        assertThat(stats.getFlow()).isEqualTo(13.0);

        stats = categoryService.getCategoryStats(category, today, Period.days(1), 0);
        assertThat(stats.getDifference()).isEqualTo(30.0);
        assertThat(stats.getFlow()).isEqualTo(210.0);

        stats = categoryService.getCategoryStats(category, today, Period.days(2), 0);
        assertThat(stats.getDifference()).isEqualTo(530.0);
        assertThat(stats.getFlow()).isEqualTo(1310.0);
    }

    @Test
    public void shouldReturnProperStats2() {
        final Category category = new Category().setType(Category.Type.INCOME_EXPENSE).setName("Test category");
        categoryService.insert(category);
        walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).setName("Wallet 1"));
        walletService.insert(new Wallet().setType(Wallet.Type.MY_WALLET).setInitialAmount(100.0).setName("Wallet 2"));

        Date today = DateTime.now().toDate();
        Date yesterday = new DateTime(today).minusDays(1).toDate();
        Date tomorrow = new DateTime(today).plusDays(1).toDate();

        // yesterday
        // -5 +7 -1 = +1
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(0)).setAmount(5.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(7.0).setDateTime(yesterday));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(0)).setAmount(1.0).setDateTime(yesterday));

        // today
        // +50 +70 -90 = +30
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(0)).setAmount(50.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(70.0).setDateTime(today));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(1)).setAmount(90.0).setDateTime(today));

        // tomorrow
        // +100 -300 +700 = 500
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(100.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setCategory(category).setFromWallet(walletService.getMyWallets().get(1)).setAmount(300.0).setDateTime(tomorrow));
        cashFlowService.insert(new CashFlow().setCategory(category).setToWallet(walletService.getMyWallets().get(1)).setAmount(700.0).setDateTime(tomorrow));

        CategoryService.CategoryStats stats;

        stats = getCategoryStats(category, yesterday, Period.days(1), 0);
        assertThat(stats.getDifference()).isEqualTo(1.0);
        assertThat(stats.getFlow()).isEqualTo(13.0);

        stats = getCategoryStats(category, yesterday, Period.days(1), 1);
        assertThat(stats.getDifference()).isEqualTo(30.0);
        assertThat(stats.getFlow()).isEqualTo(210.0);

        stats = getCategoryStats(category, yesterday, Period.days(2), 0);
        assertThat(stats.getDifference()).isEqualTo(31.0);
        assertThat(stats.getFlow()).isEqualTo(223.0);

        stats = getCategoryStats(category, today, Period.days(1), -1);
        assertThat(stats.getDifference()).isEqualTo(1.0);
        assertThat(stats.getFlow()).isEqualTo(13.0);

        stats = getCategoryStats(category, today, Period.days(1), 0);
        assertThat(stats.getDifference()).isEqualTo(30.0);
        assertThat(stats.getFlow()).isEqualTo(210.0);

        stats = getCategoryStats(category, today, Period.days(2), 0);
        assertThat(stats.getDifference()).isEqualTo(530.0);
        assertThat(stats.getFlow()).isEqualTo(1310.0);
    }

    private CategoryService.CategoryStats getCategoryStats(final Category category, Date yesterday, Period period, Integer iteration) {
        List<CategoryService.CategoryStats> categoryStatsList = categoryService.getCategoryStateList(yesterday, period, iteration);

        CategoryService.CategoryStats categoryStat = Iterables.find(categoryStatsList, new Predicate<CategoryService.CategoryStats>() {
            @Override
            public boolean apply(CategoryService.CategoryStats input) {
                return category.getId().equals(input.getCategoryId());
            }
        });
        return categoryStat;
    }
}
