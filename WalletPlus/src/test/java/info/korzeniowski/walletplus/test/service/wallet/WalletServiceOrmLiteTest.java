package info.korzeniowski.walletplus.test.service.wallet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import javax.inject.Inject;

import info.korzeniowski.walletplus.MyRobolectricTestRunner;
import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.StatisticService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import pl.wkr.fluentrule.api.FluentExpectedException;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MyRobolectricTestRunner.class)
public class WalletServiceOrmLiteTest {

    @Inject
    WalletService walletService;

    @Inject
    CashFlowService cashFlowService;

    @Inject
    StatisticService statisticService;

    @Rule
    public FluentExpectedException exception = FluentExpectedException.none();

    @Before
    public void setUp() {
        ((TestWalletPlus) RuntimeEnvironment.application).component().inject(this);
    }

    /**
     * CREATE
     */
    @Test
    public void shouldThrowExceptionWhenCreateWalletWithoutName() {
        // then
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class)
                .hasMessageContaining(Wallet.NAME_COLUMN_NAME);

        walletService.insert(new Wallet().setInitialAmount(100.0));
    }

    @Test
    public void shouldThrowExceptionWhenCreateWalletWithoutInitialAmount() {
        // then
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class)
                .hasMessageContaining(Wallet.INITIAL_AMOUNT_COLUMN_NAME);

        walletService.insert(new Wallet().setName("Wallet"));
    }

    @Test
    public void shouldInsertWallet() {
        // given
        Wallet wallet = new Wallet().setName("Wallet").setInitialAmount(100.0);

        // when
        walletService.insert(wallet);

        // then
        assertThat(walletService.findById(wallet.getId())).isEqualTo(wallet);
    }

    @Test
    public void shouldSetCurrentAmountEqualToInitialAmountAfterInsert() {
        // given
        double initialAmount = 100.0;
        double currentAmount = 500.0;
        String walletName = "Wallet";

        Wallet wallet = new Wallet()
                .setName(walletName)
                .setInitialAmount(initialAmount)
                .setCurrentAmount(currentAmount);

        // when
        walletService.insert(wallet);

        // then
        assertThat(walletService.findById(wallet.getId()).getName()).isEqualTo(walletName);
        assertThat(walletService.findById(wallet.getId()).getInitialAmount()).isEqualTo(initialAmount);
        assertThat(walletService.findById(wallet.getId()).getCurrentAmount()).isEqualTo(initialAmount);
    }

    /**
     * READ
     */
    @Test
    public void shouldGetAllWalletsOrderedByName() {
        Wallet Bartek = new Wallet().setInitialAmount(100.0).setName("Bartek");
        walletService.insert(Bartek);
        Wallet celina = new Wallet().setInitialAmount(200.0).setName("celina");
        walletService.insert(celina);
        Wallet Celina = new Wallet().setInitialAmount(100.0).setName("Celina");
        walletService.insert(Celina);
        Wallet Alicja = new Wallet().setInitialAmount(100.0).setName("Alicja");
        walletService.insert(Alicja);
        Wallet Ewa = new Wallet().setInitialAmount(100.0).setName("Ewa");
        walletService.insert(Ewa);
        Wallet alicja = new Wallet().setInitialAmount(100.0).setName("alicja");
        walletService.insert(alicja);
        Wallet celina_wielka = new Wallet().setInitialAmount(100.0).setName("celina wielka");
        walletService.insert(celina_wielka);
        Wallet daniel = new Wallet().setInitialAmount(100.0).setName("daniel");
        walletService.insert(daniel);

        assertThat(walletService.getAll()).containsExactly(
                alicja, Alicja, Bartek, celina, Celina, celina_wielka, daniel, Ewa
        );
        assertThat(walletService.count()).isEqualTo(8);
    }

    /**
     * UPDATE
     */
    @Test
    public void shouldFixCurrentAmountAfterChangeInitialAmountOfWallet() {
        // given
        double initialAmount = 100.0;
        Wallet wallet = new Wallet().setInitialAmount(initialAmount).setName("Wallet");
        walletService.insert(wallet);

        cashFlowService.insert(new CashFlow().setWallet(wallet).setAmount(50.0).setType(CashFlow.Type.INCOME));
        Double currentAmount = walletService.findById(wallet.getId()).getCurrentAmount();

        // when
        double newInitialAmount = 200.0;
        walletService.update(wallet.setInitialAmount(newInitialAmount));

        // then
        assertThat(walletService.findById(wallet.getId()).getInitialAmount())
                .isEqualTo(newInitialAmount);
        assertThat(walletService.findById(wallet.getId()).getCurrentAmount())
                .isEqualTo(currentAmount - initialAmount + newInitialAmount);
    }

    @Test
    public void shouldNotUpdateCurrentAmount() {
        // given
        double initialAmount = 100.0;
        Wallet wallet = new Wallet().setInitialAmount(initialAmount).setName("Wallet");
        walletService.insert(wallet);

        // when
        walletService.update(wallet.setCurrentAmount(initialAmount + 100.0));

        // then
        assertThat(walletService.findById(wallet.getId()).getInitialAmount()).isEqualTo(initialAmount);
        assertThat(walletService.findById(wallet.getId()).getCurrentAmount()).isEqualTo(initialAmount);
    }

    /**
     * DELETE
     */
    @Test
    public void shouldDeleteWallet() {
        // given
        Wallet wallet = new Wallet().setName("Wallet").setInitialAmount(100.0);
        walletService.insert(wallet);

        // when
        walletService.deleteById(wallet.getId());

        // then
        assertThat(walletService.findById(wallet.getId())).isNull();
    }

    @Test
    public void shouldDeleteRelatedCashFlowsAfterDelete() {
        // given
        Wallet wallet1 = new Wallet().setName("Wallet 1").setInitialAmount(100.0);
        walletService.insert(wallet1);
        Wallet wallet2 = new Wallet().setName("Wallet 2").setInitialAmount(200.0);
        walletService.insert(wallet2);

        CashFlow cashFlow1 = new CashFlow().setWallet(wallet1).setAmount(10.0).setType(CashFlow.Type.INCOME);
        cashFlowService.insert(cashFlow1);
        CashFlow cashFlow2 = new CashFlow().setWallet(wallet2).setAmount(10.0).setType(CashFlow.Type.EXPANSE);
        cashFlowService.insert(cashFlow2);
        CashFlow cashFlow3 = new CashFlow().setWallet(wallet2).setAmount(10.0).setType(CashFlow.Type.INCOME);
        cashFlowService.insert(cashFlow3);
        CashFlow cashFlow4 = new CashFlow().setWallet(wallet1).setAmount(10.0).setType(CashFlow.Type.EXPANSE);
        cashFlowService.insert(cashFlow4);

        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet1.getId())).isEqualTo(2);

        // when
        walletService.deleteById(wallet1.getId());

        // then
        assertThat(statisticService.countCashFlowsAssignedToWallet(wallet1.getId())).isEqualTo(0);
        assertThat(cashFlowService.findById(cashFlow1.getId())).isNull();
        assertThat(cashFlowService.findById(cashFlow2.getId())).isEqualTo(cashFlow2);
        assertThat(cashFlowService.findById(cashFlow3.getId())).isEqualTo(cashFlow3);
        assertThat(cashFlowService.findById(cashFlow4.getId())).isNull();
    }
}
