package info.korzeniowski.walletplus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

public class Wallet implements Identifiable, Parcelable {
    public static final Parcelable.Creator<Wallet> CREATOR = new Parcelable.Creator<Wallet>() {
        public Wallet createFromParcel(Parcel in) {
            return new Wallet(in);
        }

        public Wallet[] newArray(int size) {
            return new Wallet[size];
        }
    };
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private Type type;

    @DatabaseField(canBeNull = false)
    private Double initialAmount;

    @DatabaseField(canBeNull = false)
    private Double currentAmount;

    public Wallet() {

    }

    private Wallet(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
        type = Type.values()[parcel.readInt()];
        initialAmount = parcel.readDouble();
        currentAmount = parcel.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(type.ordinal());
        dest.writeDouble(initialAmount);
        dest.writeDouble(currentAmount);
    }

    @Override
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

    public Type getType() {
        return type;
    }

    public Wallet setType(Type type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Wallet wallet = (Wallet) o;

        if (currentAmount != null ? !currentAmount.equals(wallet.currentAmount) : wallet.currentAmount != null)
            return false;
        if (id != null ? !id.equals(wallet.id) : wallet.id != null)
            return false;
        if (initialAmount != null ? !initialAmount.equals(wallet.initialAmount) : wallet.initialAmount != null)
            return false;
        if (name != null ? !name.equals(wallet.name) : wallet.name != null)
            return false;
        if (type != wallet.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (initialAmount != null ? initialAmount.hashCode() : 0);
        result = 31 * result + (currentAmount != null ? currentAmount.hashCode() : 0);
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

    public enum Type {
        MY_WALLET,
        CONTRACTOR
    }
}
