package info.korzeniowski.walletplus.ui.wallet.details;

import android.os.Parcel;
import android.os.Parcelable;

import info.korzeniowski.walletplus.model.Wallet;

public class WalletDetailsParcelableState implements Parcelable {
    private Long id;
    private String name;
    private String originalName;
    private Wallet.Type type;
    private Double initialAmount;
    private Double currentAmount;

    public static final Parcelable.Creator<WalletDetailsParcelableState> CREATOR = new Parcelable.Creator<WalletDetailsParcelableState>() {
        public WalletDetailsParcelableState createFromParcel(Parcel in) {
            return new WalletDetailsParcelableState(in);
        }

        public WalletDetailsParcelableState[] newArray(int size) {
            return new WalletDetailsParcelableState[size];
        }
    };

    public Wallet getWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(getId());
        wallet.setName(getName());
        wallet.setType(getType());
        wallet.setInitialAmount(getInitialAmount());
        wallet.setCurrentAmount(getCurrentAmount());
        return wallet;
    }

    public WalletDetailsParcelableState(Parcel in) {
        id = in.readLong();
        name = in.readString();
        originalName = in.readString();
        type = Wallet.Type.values()[in.readInt()];
        initialAmount = in.readDouble();
        currentAmount = in.readDouble();
    }

    public WalletDetailsParcelableState(Wallet wallet) {
        if (wallet != null) {
            id = wallet.getId();
            name = wallet.getName();
            originalName = wallet.getName();
            type = wallet.getType();
            initialAmount = wallet.getInitialAmount();
            currentAmount = wallet.getCurrentAmount();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(originalName);
        dest.writeInt(type.ordinal());
        dest.writeDouble(initialAmount);
        dest.writeDouble(currentAmount);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Wallet.Type getType() {
        return type;
    }

    public void setType(Wallet.Type type) {
        this.type = type;
    }

    public Double getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(Double initialAmount) {
        this.initialAmount = initialAmount;
    }

    public Double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Double currentAmount) {
        this.currentAmount = currentAmount;
    }
}
