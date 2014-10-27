package info.korzeniowski.walletplus.ui.wallet.details;

import android.os.Parcel;
import android.os.Parcelable;

public class WalletDetailsParcelableState implements Parcelable {
    public static final Parcelable.Creator<WalletDetailsParcelableState> CREATOR = new Parcelable.Creator<WalletDetailsParcelableState>() {
        public WalletDetailsParcelableState createFromParcel(Parcel in) {
            return new WalletDetailsParcelableState(in);
        }

        public WalletDetailsParcelableState[] newArray(int size) {
            return new WalletDetailsParcelableState[size];
        }
    };

    private Long id;
    private String name;
    private Double initialAmount;
    private Double currentAmount;

    public WalletDetailsParcelableState() {

    }

    public WalletDetailsParcelableState(Parcel in) {
        id = (Long) in.readValue(Long.class.getClassLoader());
        name = in.readString();
        initialAmount = (Double) in.readValue(Double.class.getClassLoader());
        currentAmount = (Double) in.readValue(Double.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeString(name);
        dest.writeValue(initialAmount);
        dest.writeValue(currentAmount);
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
