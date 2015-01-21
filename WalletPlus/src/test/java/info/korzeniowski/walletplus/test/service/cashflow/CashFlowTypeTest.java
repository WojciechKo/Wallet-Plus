package info.korzeniowski.walletplus.test.service.cashflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CashFlowTypeTest {
    private Wallet myWallet;
    private Wallet contractorWallet;

    @Before
    public void setUp() {
        myWallet = new Wallet();
        myWallet.setName("My wallet").setType(Wallet.Type.MY_WALLET);

        contractorWallet = new Wallet();
        contractorWallet.setName("Contractor wallet").setType(Wallet.Type.OTHER);
    }

    @Test
    public void shouldReturnIncomeType() {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setFromWallet(contractorWallet).setToWallet(myWallet);
        assertThat(cashFlow.getType()).isEqualTo(CashFlow.Type.INCOME);

        cashFlow.setFromWallet(null).setToWallet(myWallet);
        assertThat(cashFlow.getType()).isEqualTo(CashFlow.Type.INCOME);
    }

    @Test
    public void shouldReturnExpanseType() {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setFromWallet(myWallet).setToWallet(contractorWallet);
        assertThat(cashFlow.getType()).isEqualTo(CashFlow.Type.EXPANSE);

        cashFlow.setFromWallet(myWallet).setToWallet(null);
        assertThat(cashFlow.getType()).isEqualTo(CashFlow.Type.EXPANSE);
    }

    @Test
    public void shouldReturnTransferType() {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setFromWallet(myWallet).setToWallet(myWallet);
        assertThat(cashFlow.getType()).isEqualTo(CashFlow.Type.TRANSFER);
    }

    @Test
    public void shouldThrowExceptionWhenUnknownType() {
        testCashFlowTypeExceptionWithGivenFromAndToWallets(contractorWallet, contractorWallet);
        testCashFlowTypeExceptionWithGivenFromAndToWallets(contractorWallet, null);
        testCashFlowTypeExceptionWithGivenFromAndToWallets(null, contractorWallet);
        testCashFlowTypeExceptionWithGivenFromAndToWallets(null, null);
    }

    private void testCashFlowTypeExceptionWithGivenFromAndToWallets(Wallet fromWallet, Wallet toWallet) {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setFromWallet(fromWallet).setToWallet(toWallet);
        try {
            cashFlow.getType();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Unknown type of CashFlow");
        }
    }
}
