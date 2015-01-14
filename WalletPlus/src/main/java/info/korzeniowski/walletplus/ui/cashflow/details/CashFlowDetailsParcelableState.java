package info.korzeniowski.walletplus.ui.cashflow.details;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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

    private static final CashFlow.Type defaultType = CashFlow.Type.EXPANSE;

    private Long id;
    private String amount;
    private String comment;
    private Wallet myFirstWallet;
    private Wallet mySecondWallet;
    private Wallet otherWallet;
    private Category category;
    private CashFlow.Type type;
    private CashFlow.Type previousType;
    private Long date;

    public CashFlowDetailsParcelableState() {
        setDate(Calendar.getInstance().getTimeInMillis());
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
            setCategory(cashFlow.getCategory());
            otherWallet = cashFlow.getFromWallet();
            mySecondWallet = cashFlow.getToWallet();

        } else if (getType() == CashFlow.Type.EXPANSE) {
            setCategory(cashFlow.getCategory());
            myFirstWallet = cashFlow.getFromWallet();
            otherWallet = cashFlow.getToWallet();

        } else if (getType() == CashFlow.Type.TRANSFER) {
            setCategory(null);
            myFirstWallet = cashFlow.getFromWallet();
            mySecondWallet = cashFlow.getToWallet();
        }
    }

    private CashFlowDetailsParcelableState(Parcel in) {
        id = (Long) in.readValue(Long.class.getClassLoader());
        amount = in.readString();
        comment = in.readString();
        otherWallet = in.readParcelable(Wallet.class.getClassLoader());
        myFirstWallet = in.readParcelable(Wallet.class.getClassLoader());
        mySecondWallet = in.readParcelable(Wallet.class.getClassLoader());
        otherWallet = in.readParcelable(Wallet.class.getClassLoader());
        category = in.readParcelable(Category.class.getClassLoader());
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
        dest.writeParcelable(otherWallet, flags);
        dest.writeParcelable(myFirstWallet, flags);
        dest.writeParcelable(mySecondWallet, flags);
        dest.writeParcelable(otherWallet, flags);
        dest.writeParcelable(category, flags);
        dest.writeValue(type != null ? type.ordinal() : null);
        dest.writeValue(previousType != null ? previousType.ordinal() : null);
        dest.writeValue(date);
    }

    public void swapWallets() {
        Wallet savedFirst = myFirstWallet;
        myFirstWallet = mySecondWallet;
        mySecondWallet = savedFirst;
        if (getType() == CashFlow.Type.INCOME) {
            setType(CashFlow.Type.EXPANSE);
        } else if (getType() == CashFlow.Type.EXPANSE) {
            setType(CashFlow.Type.INCOME);
        }
    }

    public CashFlow buildCashFlow() {
        CashFlow cashFlow = new CashFlow();

        cashFlow.setId(getId());
        cashFlow.setAmount(Double.parseDouble(getAmount()));
        cashFlow.setDateTime(new Date(getDate()));
        cashFlow.setComment(getComment());
        cashFlow.setFromWallet(getFromWallet());
        cashFlow.setToWallet(getToWallet());
        cashFlow.setCategory(getCategory());

        return cashFlow;
    }

    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isAmountValid() {
        if ("-".equals(amount) || "+".equals(amount)) {
            return true;
        }
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CashFlow.Type getType() {
        return type;
    }

    public void setType(CashFlow.Type type) {
        if (getType() != type) {
            this.previousType = getType();
            this.type = type;
        }
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

    public Wallet getToWallet() {
        if (getType() == CashFlow.Type.EXPANSE) {
            return otherWallet;
        } else if (Arrays.asList(CashFlow.Type.INCOME, CashFlow.Type.TRANSFER).contains(getType())) {
            return mySecondWallet;
        }
        return null;
    }

    public void setToWallet(Wallet toWallet) {
        if (getType() == CashFlow.Type.EXPANSE) {
            otherWallet = toWallet;
        } else if (Arrays.asList(CashFlow.Type.INCOME, CashFlow.Type.TRANSFER).contains(getType())) {
            mySecondWallet = toWallet;
        }
    }

    public Wallet getFromWallet() {
        if (CashFlow.Type.INCOME.equals(getType())) {
            return otherWallet;
        } else if (Arrays.asList(CashFlow.Type.EXPANSE, CashFlow.Type.TRANSFER).contains(getType())) {
            return myFirstWallet;
        }
        return null;
    }

    public void setFromWallet(Wallet fromWallet) {
        if (getType() == CashFlow.Type.INCOME) {
            otherWallet = fromWallet;
        } else if (Arrays.asList(CashFlow.Type.EXPANSE, CashFlow.Type.TRANSFER).contains(getType())) {
            myFirstWallet = fromWallet;
        }
    }

    public Category getCategory() {
        if (Arrays.asList(CashFlow.Type.INCOME, CashFlow.Type.EXPANSE).contains(getType())) {
            return category;
        }
        return null;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
