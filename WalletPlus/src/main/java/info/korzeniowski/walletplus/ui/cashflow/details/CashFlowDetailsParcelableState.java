package info.korzeniowski.walletplus.ui.cashflow.details;

import android.os.Parcel;
import android.os.Parcelable;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;

public class CashFlowDetailsParcelableState implements Parcelable {
    public static final Parcelable.Creator<CashFlowDetailsParcelableState> CREATOR = new Parcelable.Creator<CashFlowDetailsParcelableState>() {
        public CashFlowDetailsParcelableState createFromParcel(Parcel in) {
            return new CashFlowDetailsParcelableState(in);
        }

        public CashFlowDetailsParcelableState[] newArray(int size) {
            return new CashFlowDetailsParcelableState[size];
        }
    };

    private Long id;
    private Float amount;
    private String comment = "";
    private Wallet incomeFromWallet;
    private Wallet incomeToWallet;
    private Wallet expanseFromWallet;
    private Wallet expanseToWallet;
    private Category incomeCategory;
    private Category expanseCategory;
    private CashFlow.Type type;
    private Long date;

    public CashFlowDetailsParcelableState() {

    }

    public CashFlowDetailsParcelableState(Parcel in) {
        id = (Long) in.readValue(Long.class.getClassLoader());
        amount = (Float) in.readValue(Float.class.getClassLoader());
        comment = in.readString();
        incomeFromWallet = in.readParcelable(Wallet.class.getClassLoader());
        incomeToWallet = in.readParcelable(Wallet.class.getClassLoader());
        expanseFromWallet = in.readParcelable(Wallet.class.getClassLoader());
        expanseToWallet = in.readParcelable(Wallet.class.getClassLoader());
        incomeCategory = in.readParcelable(Category.class.getClassLoader());
        expanseCategory = in.readParcelable(Category.class.getClassLoader());
        Integer typeOrdinal = (Integer) in.readValue(Integer.class.getClassLoader());
        type = typeOrdinal != null ? CashFlow.Type.values()[typeOrdinal] : null;
        date = (Long) in.readValue(Long.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(amount);
        dest.writeString(comment);
        dest.writeParcelable(incomeFromWallet, flags);
        dest.writeParcelable(incomeToWallet, flags);
        dest.writeParcelable(expanseFromWallet, flags);
        dest.writeParcelable(expanseToWallet, flags);
        dest.writeParcelable(incomeCategory, flags);
        dest.writeParcelable(expanseCategory, flags);
        dest.writeValue(type != null ? type.ordinal() : null);
        dest.writeValue(date);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Wallet getIncomeFromWallet() {
        return incomeFromWallet;
    }

    public void setIncomeFromWallet(Wallet incomeFromWallet) {
        this.incomeFromWallet = incomeFromWallet;
    }

    public Wallet getIncomeToWallet() {
        return incomeToWallet;
    }

    public void setIncomeToWallet(Wallet incomeToWallet) {
        this.incomeToWallet = incomeToWallet;
    }

    public Wallet getExpanseFromWallet() {
        return expanseFromWallet;
    }

    public void setExpanseFromWallet(Wallet expanseFromWallet) {
        this.expanseFromWallet = expanseFromWallet;
    }

    public Wallet getExpanseToWallet() {
        return expanseToWallet;
    }

    public void setExpanseToWallet(Wallet expanseToWallet) {
        this.expanseToWallet = expanseToWallet;
    }

    public Category getIncomeCategory() {
        return incomeCategory;
    }

    public void setIncomeCategory(Category incomeCategory) {
        this.incomeCategory = incomeCategory;
    }

    public Category getExpanseCategory() {
        return expanseCategory;
    }

    public void setExpanseCategory(Category expanseCategory) {
        this.expanseCategory = expanseCategory;
    }

    public CashFlow.Type getType() {
        return type;
    }

    public void setType(CashFlow.Type type) {
        this.type = type;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
