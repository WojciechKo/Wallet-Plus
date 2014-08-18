package info.korzeniowski.walletplus.ui.cashflow;

import java.util.Calendar;

import info.korzeniowski.walletplus.model.Wallet;

public interface OnCashFlowDetailsChangedListener {
    public void onAmountChanged();
    public void onCommentChanged();
    public void onCategoryChanged();
    public void onFromWalletChanged();
    public void onToWalletChanged();
    public void onDateChanged();
    public void onTimeChanged();
}
