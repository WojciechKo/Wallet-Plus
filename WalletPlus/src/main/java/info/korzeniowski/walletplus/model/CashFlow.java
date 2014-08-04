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

    /** ORMLite requirement **/
    public CashFlow() {

    }

    public CashFlow(Builder builder) {
        id = builder.id;
        fromWallet = builder.fromWallet;
        toWallet = builder.toWallet;
        category = builder.category;
        amount = builder.amount;
        dateTime = builder.dateTime;
        comment = builder.comment;
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

    public static class Builder {
        private Long id;
        private Wallet fromWallet;
        private Wallet toWallet;
        private Category category;
        private Float amount;
        private Date dateTime;
        private String comment;

        public Builder() {

        }

        public Builder(CashFlow cashFlow) {
            id = cashFlow.getId();
            fromWallet = cashFlow.getFromWallet();
            toWallet = cashFlow.getToWallet();
            category = cashFlow.getCategory();
            amount = cashFlow.getAmount();
            dateTime = cashFlow.getDateTime();
            comment = cashFlow.getComment();
        }

        public Long getId() {
            return id;
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Wallet getFromWallet() {
            return fromWallet;
        }

        public Builder setFromWallet(Wallet fromWallet) {
            this.fromWallet = fromWallet;
            return this;
        }

        public Wallet getToWallet() {
            return toWallet;
        }

        public Builder setToWallet(Wallet toWallet) {
            this.toWallet = toWallet;
            return this;
        }

        public Category getCategory() {
            return category;
        }

        public Builder setCategory(Category category) {
            this.category = category;
            return this;
        }

        public Float getAmount() {
            return amount;
        }

        public Builder setAmount(Float amount) {
            this.amount = amount;
            return this;
        }

        public Date getDateTime() {
            return dateTime;
        }

        public Builder setDateTime(Date dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public String getComment() {
            return comment;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public CashFlow build() {
            return new CashFlow(this);
        }
    }
}
