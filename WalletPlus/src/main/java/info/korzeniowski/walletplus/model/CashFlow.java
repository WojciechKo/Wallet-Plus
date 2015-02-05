package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class CashFlow implements Identifiable {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Wallet fromWallet;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Wallet toWallet;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "integer REFERENCES category(id) ON DELETE SET NULL")
    private Category category;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "integer REFERENCES event(id) ON DELETE SET NULL")
    private Event event;

    @DatabaseField(canBeNull = false)
    private Double amount;

    @DatabaseField(canBeNull = false)
    private Date dateTime;

    @DatabaseField
    private String comment;

    @DatabaseField(canBeNull = false)
    private boolean completed;

    @Override
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

    public Event getEvent() {
        return event;
    }

    public CashFlow setEvent(Event event) {
        this.event = event;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public CashFlow setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public CashFlow setDateTime(Date dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public CashFlow setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Type getType() {
        if (getFromWallet() != null && getFromWallet().getType() == Wallet.Type.MY_WALLET) {
            if (getToWallet() != null && getToWallet().getType() == Wallet.Type.MY_WALLET) {
                return Type.TRANSFER;
            } else if (getToWallet() == null || getToWallet().getType() == Wallet.Type.OTHER) {
                return Type.EXPANSE;
            } else {
                throw new RuntimeException("Unknown type of CashFlow");
            }
        } else if (getToWallet() != null && getToWallet().getType() == Wallet.Type.MY_WALLET) {
            if (getFromWallet() == null || getFromWallet().getType() == Wallet.Type.OTHER) {
                return Type.INCOME;
            }
        }
        throw new RuntimeException("Unknown type of CashFlow");
    }

    public enum Type {
        INCOME,
        EXPANSE,
        TRANSFER
    }
}
