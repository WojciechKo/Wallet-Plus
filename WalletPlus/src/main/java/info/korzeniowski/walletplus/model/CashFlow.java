package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class CashFlow {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Wallet fromWallet;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Wallet toWallet;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Category category;

    @DatabaseField(canBeNull = false)
    private Float amount;

    @DatabaseField(canBeNull = false)
    private Date dateTime;

    @DatabaseField
    private String comment;

    public CashFlow() {

    }

    public CashFlow(Float amount, Date dateTime) {
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public Long getId() {
        return id;
    }

    public CashFlow setId(Long id) {
        this.id = id;
        return this;
    }

    public Wallet getFromWallet() {
        return fromWallet;
    }

    public CashFlow setFromWallet(Wallet fromWallet) {
        this.fromWallet = fromWallet;
        return this;
    }

    public Wallet getToWallet() {
        return toWallet;
    }

    public CashFlow setToWallet(Wallet toWallet) {
        this.toWallet = toWallet;
        return this;
    }

    public Category getCategory() {
        return category;
    }

    public CashFlow setCategory(Category category) {
        this.category = category;
        return this;
    }

    public Float getAmount() {
        return amount;
    }

    public CashFlow setAmount(Float amount) {
        this.amount = amount;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public CashFlow setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public CashFlow setDateTime(Date dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public boolean isExpanse() {
        if (getFromWallet() != null && getFromWallet().getType() == Wallet.Type.MY_WALLET) {
            if (getToWallet() == null) {
                return true;
            } else if (getToWallet().getType() == Wallet.Type.CONTRACTOR) {
                return true;
            }
        }
        return false;
    }

    public boolean isIncome() {
        if (getToWallet() != null && getToWallet().getType() == Wallet.Type.MY_WALLET) {
            if (getFromWallet() == null) {
                return true;
            } else if (getFromWallet().getType() == Wallet.Type.CONTRACTOR) {
                return true;
            }
        }
        return false;
    }

    public boolean isTransfer() {
        return getFromWallet() != null && getFromWallet().getType() == Wallet.Type.MY_WALLET &&
                getToWallet() != null && getToWallet().getType() == Wallet.Type.MY_WALLET;
    }

}
