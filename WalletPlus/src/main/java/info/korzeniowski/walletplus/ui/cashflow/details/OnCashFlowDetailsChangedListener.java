package info.korzeniowski.walletplus.ui.cashflow.details;

public interface OnCashFlowDetailsChangedListener {
    public void onAmountChanged();

    public void onCommentChanged();

    public void onCategoryChanged();

    public void onFromWalletChanged();

    public void onToWalletChanged();

    public void onDateChanged();

    public void onTimeChanged();
}
