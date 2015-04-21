package com.walletudo.service.statistic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.MyRobolectricTestRunner;
import pl.net.korzeniowski.walletplus.TestWalletPlus;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Tag;
import com.walletudo.model.Wallet;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.StatisticService;
import com.walletudo.service.TagService;
import com.walletudo.service.WalletService;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MyRobolectricTestRunner.class)
public class StatisticServiceOrmLiteTest {

    @Inject
    CashFlowService cashFlowService;

    @Inject
    WalletService walletService;

    @Inject
    TagService tagService;

    @Inject
    StatisticService statisticService;

    @Before
    public void setUp() {
        ((TestWalletPlus) RuntimeEnvironment.application).component().inject(this);
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
}
