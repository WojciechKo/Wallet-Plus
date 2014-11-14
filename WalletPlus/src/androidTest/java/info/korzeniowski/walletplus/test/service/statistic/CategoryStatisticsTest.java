package info.korzeniowski.walletplus.test.service.statistic;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryStatisticsTest {


    @Inject
    @Named("local")
    StatisticService statisticService;

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

        Date yesterday = DateTime.now().minusDays(1).toDate();
        Date today = DateTime.now().toDate();
        Date tomorrow = DateTime.now().plusDays(1).toDate();

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

        List<Map.Entry<Category, Double>> stats = statisticService.getCategoryListWit(Category.Type.INCOME_EXPENSE, StatisticService.Period.DAY, today, -1);

        Map.Entry<Category, Double> found = null;
        for (Map.Entry<Category, Double> entry : stats) {
            if (entry.getKey().equals(category)) {
                found = entry;
                break;
            }
        }
        assertThat(found.getValue()).isEqualTo(1.0);

        stats = statisticService.getCategoryListWit(Category.Type.INCOME_EXPENSE, StatisticService.Period.DAY, today, 0);

        found = null;
        for (Map.Entry<Category, Double> entry : stats) {
            if (entry.getKey().equals(category)) {
                found = entry;
                break;
            }
        }
        assertThat(found.getValue()).isEqualTo(30.0);

        stats = statisticService.getCategoryListWit(Category.Type.INCOME_EXPENSE, StatisticService.Period.DAY, today, 0);

        found = null;
        for (Map.Entry<Category, Double> entry : stats) {
            if (entry.getKey().equals(category)) {
                found = entry;
                break;
            }
        }
        assertThat(found.getValue()).isEqualTo(500.0);
    }
}
