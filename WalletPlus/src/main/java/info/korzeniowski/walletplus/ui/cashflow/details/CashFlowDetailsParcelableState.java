package info.korzeniowski.walletplus.ui.cashflow.details;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

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

    public static final CashFlow.Type defaultType = CashFlow.Type.INCOME;

    private Long id;
    private String amount;
    private String comment = "";
    private Wallet incomeFromWallet;
    private Wallet incomeToWallet;
    private Wallet expanseFromWallet;
    private Wallet expanseToWallet;
    private Category incomeCategory;
    private Category expanseCategory;
    private CashFlow.Type type;
    private CashFlow.Type previousType;
    private Long date;

    public CashFlowDetailsParcelableState() {
        setDate(Calendar.getInstance().getTimeInMillis());
        setIncomeCategory(null);
        setExpanseCategory(null);
        setType(defaultType);
        this.previousType = defaultType;
    }

    public CashFlowDetailsParcelableState(CashFlow cashFlow) {
        setId(cashFlow.getId());
        setAmount(cashFlow.getAmount().toString());
        setComment(cashFlow.getComment());
        setDate(cashFlow.getDateTime().getTime());
        setType(cashFlow.getType());
        this.previousType = defaultType;

        if (getType() == CashFlow.Type.INCOME) {
            setIncomeCategory(cashFlow.getCategory());
            setExpanseCategory(null);
            setIncomeFromWallet(cashFlow.getFromWallet());
            setIncomeToWallet(cashFlow.getToWallet());
        } else if (getType() == CashFlow.Type.EXPANSE) {
            setIncomeCategory(null);
            setExpanseCategory(cashFlow.getCategory());
            setExpanseFromWallet(cashFlow.getFromWallet());
            setExpanseToWallet(cashFlow.getToWallet());
        } else if (getType() == CashFlow.Type.TRANSFER) {
            setIncomeCategory(null);
            setExpanseCategory(null);
            setExpanseFromWallet(cashFlow.getFromWallet());
            setIncomeToWallet(cashFlow.getToWallet());
        }
    }

    public CashFlowDetailsParcelableState(Parcel in) {
        id = (Long) in.readValue(Long.class.getClassLoader());
        amount = in.readString();
        comment = in.readString();
        incomeFromWallet = in.readParcelable(Wallet.class.getClassLoader());
        incomeToWallet = in.readParcelable(Wallet.class.getClassLoader());
        expanseFromWallet = in.readParcelable(Wallet.class.getClassLoader());
        expanseToWallet = in.readParcelable(Wallet.class.getClassLoader());
        incomeCategory = in.readParcelable(Category.class.getClassLoader());
        expanseCategory = in.readParcelable(Category.class.getClassLoader());
        Integer typeOrdinal = (Integer) in.readValue(Integer.class.getClassLoader());
        type = typeOrdinal != null ? CashFlow.Type.values()[typeOrdinal] : null;
        Integer previousTypeOrdinal = (Integer) in.readValue(Integer.class.getClassLoader());
        previousType = previousTypeOrdinal != null ? CashFlow.Type.values()[previousTypeOrdinal] : null;
        date = (Long) in.readValue(Long.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeString(amount);
        dest.writeString(comment);
        dest.writeParcelable(incomeFromWallet, flags);
        dest.writeParcelable(incomeToWallet, flags);
        dest.writeParcelable(expanseFromWallet, flags);
        dest.writeParcelable(expanseToWallet, flags);
        dest.writeParcelable(incomeCategory, flags);
        dest.writeParcelable(expanseCategory, flags);
        dest.writeValue(type != null ? type.ordinal() : null);
        dest.writeValue(previousType != null ? previousType.ordinal() : null);
        dest.writeValue(date);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
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
        if (getType() != type) {
            this.previousType = getType();
        }
        this.type = type;
    }

    public CashFlow.Type getPreviousType() {
        return previousType;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
