package info.korzeniowski.walletplus.ui.wallet.list;

class DeleteWalletEvent {
    private Long id;

    public DeleteWalletEvent(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
