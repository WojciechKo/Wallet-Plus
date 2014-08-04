package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.List;

public class Wallet {
    public enum Type {MY_WALLET, CONTRACTOR}

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(uniqueIndex = true, canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private Type type;

    @DatabaseField(canBeNull = false)
    private Double initialAmount;

    @DatabaseField(canBeNull = false)
    private Double currentAmount;

    /** ORMLite requirement **/
    public Wallet() {

    }

    private Wallet(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.initialAmount = builder.initialAmount;

        if (builder.currentAmount == null) {
            this.currentAmount = builder.initialAmount;
        } else {
            this.currentAmount = builder.currentAmount;
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getInitialAmount() {
        return initialAmount;
    }

    public Double getCurrentAmount() {
        return currentAmount;
    }

    public Type getType() {
        return type;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wallet wallet = (Wallet) o;

        if (id != null ? !id.equals(wallet.id) : wallet.id != null) return false;
        if (name != null ? !name.equals(wallet.name) : wallet.name != null) return false;
        if (type != wallet.type) return false;

        return true;
    }

    @Override
    public final int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", initialAmount=" + initialAmount +
                ", currentAmount=" + currentAmount +
                '}';
    }

    public static class Builder {
        private Long id;
        private String name;
        private Type type;
        private Double initialAmount;
        private Double currentAmount;

        public Builder() {

        }

        public Builder(Wallet wallet) {
            if (wallet != null) {
                id = wallet.getId();
                name = wallet.getName();
                type = wallet.getType();
                initialAmount = wallet.getInitialAmount();
                currentAmount = wallet.getCurrentAmount();
            }
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setInitialAmount(Double initialAmount) {
            this.initialAmount = initialAmount;
            return this;
        }

        public Builder setCurrentAmount(Double currentAmount) {
            this.currentAmount = currentAmount;
            return this;
        }

        public Wallet build() {
            return new Wallet(this);
        }
    }
}
