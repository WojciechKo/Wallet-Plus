package info.korzeniowski.walletplus.model;

import java.util.List;

public class Wallet {
    public enum Type {MY_WALLET, CONTRACTOR;}

    private Long id;
    private String name;
    private Double initialAmount;
    private Double currentAmount;
    private Type type;

    public Wallet() {
    }

    public Long getId() {
        return id;
    }

    public Wallet setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Wallet setName(String name) {
        this.name = name;
        return this;
    }

    public Double getInitialAmount() {
        return initialAmount;
    }

    public Wallet setInitialAmount(Double initialAmount) {
        this.initialAmount = initialAmount;
        return this;
    }

    public Double getCurrentAmount() {
        return currentAmount;
    }

    public Wallet setCurrentAmount(Double currentAmount) {
        this.currentAmount = currentAmount;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Wallet setType(Type type) {
        this.type = type;
        return this;
    }

    public static Wallet findById(List<Wallet> wallets, Long id) {
        for (Wallet wallet : wallets) {
            if (id.equals(wallet.getId())) {
                return wallet;
            }
        }
        return null;
    }
}
