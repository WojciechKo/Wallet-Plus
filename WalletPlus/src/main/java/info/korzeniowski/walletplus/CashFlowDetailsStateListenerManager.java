package info.korzeniowski.walletplus;

import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowDetailsStateListener;

public interface CashFlowDetailsStateListenerManager {
    public void addCashFlowDetailsStateListener(CashFlowDetailsStateListener fragment);

    public void removeCashFlowDetailsStateListener(CashFlowDetailsStateListener fragment);

    public void cashFlowStateChanged(CashFlowDetailsStateListener notifierFragment);
}
