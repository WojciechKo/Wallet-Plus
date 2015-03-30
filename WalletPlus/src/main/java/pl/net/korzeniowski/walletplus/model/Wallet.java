package pl.net.korzeniowski.walletplus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Wallet.TABLE_NAME)
public class Wallet implements Identifiable, Parcelable {

    public static final String TABLE_NAME = "wallet";

    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String INITIAL_AMOUNT_COLUMN_NAME = "initialAmount";
    public static final String CURRENT_AMOUNT_COLUMN_NAME = "currentAmount";

    public static final Parcelable.Creator<Wallet> CREATOR = new Parcelable.Creator<Wallet>() {
        public Wallet createFromParcel(Parcel in) {
            return new Wallet(in);
        }

        public Wallet[] newArray(int size) {
            return new Wallet[size];
        }
    };

    @DatabaseField(columnName = ID_COLUMN_NAME, generatedId = true)
    private Long id;

    @DatabaseField(columnName = NAME_COLUMN_NAME, canBeNull = false)
    private String name;

    @DatabaseField(columnName = INITIAL_AMOUNT_COLUMN_NAME, canBeNull = false)
    private Double initialAmount;

    @DatabaseField(columnName = CURRENT_AMOUNT_COLUMN_NAME, canBeNull = false)
    private Double currentAmount;

    public Wallet() {

    }

    private Wallet(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
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

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (initialAmount != null ? initialAmount.hashCode() : 0);
        result = 31 * result + (currentAmount != null ? currentAmount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", initialAmount=" + initialAmount +
                ", currentAmount=" + currentAmount +
                '}';
    }

}
