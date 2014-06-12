package info.korzeniowski.walletplus.datamanager.local.modelfactory;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.greendao.GreenCashFlow;

public class CashFlowFactory {

    public static CashFlow createCashFlow(GreenCashFlow greenCashFlow) {
        if (greenCashFlow == null) {
            return null;
        }

        CashFlow cashFlow= new CashFlow();
        cashFlow.setId(greenCashFlow.getId());
        cashFlow.setAmount(greenCashFlow.getAmount());
        cashFlow.setCategoryId(greenCashFlow.getCategoryId());
        cashFlow.setComment(greenCashFlow.getComment());
        cashFlow.setDateTime(greenCashFlow.getDateTime());

        return cashFlow;
    }

    public static GreenCashFlow createGreenCashFlow(CashFlow cashFlow) {
        if (cashFlow == null) {
            return null;
        }

        GreenCashFlow greenCashFlow = new GreenCashFlow();
        greenCashFlow.setId(cashFlow.getId());
        greenCashFlow.setAmount(cashFlow.getAmount());
        greenCashFlow.setCategoryId(cashFlow.getCategoryId());
        greenCashFlow.setComment(cashFlow.getComment());
        greenCashFlow.setDateTime(cashFlow.getDateTime());

        return greenCashFlow;
    }
}
