package info.korzeniowski.walletplus.ui.cashflow.details;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.Wallet;

import static com.google.common.base.Preconditions.checkNotNull;

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
    private Wallet wallet;
    private String categories;
    private CashFlow.Type type;
    private CashFlow.Type previousType;
    private Long date;
    private Boolean completed;

    public CashFlowDetailsParcelableState() {
        setCategories("");
        setDate(Calendar.getInstance().getTimeInMillis());
        setType(defaultType);
        this.previousType = defaultType;
        setCompleted(true);
    }

    public CashFlowDetailsParcelableState(CashFlow cashFlow) {
        setId(cashFlow.getId());
        StringBuilder sb = new StringBuilder();
        for (Tag tag : cashFlow.getTags()) {
            sb.append(tag.getName()).append(" ");
        }
        setCategories(sb.toString());
        setAmount(cashFlow.getAmount().toString());
        setComment(cashFlow.getComment());
        setDate(cashFlow.getDateTime().getTime());
        setType(cashFlow.getType());
        setCompleted(cashFlow.isCompleted());
        setWallet(cashFlow.getWallet());
    }

    private CashFlowDetailsParcelableState(Parcel in) {
        id = (Long) in.readValue(Long.class.getClassLoader());
        amount = in.readString();
        comment = in.readString();
        wallet = in.readParcelable(Wallet.class.getClassLoader());
        categories = in.readString();
        Integer typeOrdinal = (Integer) in.readValue(Integer.class.getClassLoader());
        type = typeOrdinal != null ? CashFlow.Type.values()[typeOrdinal] : null;
        Integer previousTypeOrdinal = (Integer) in.readValue(Integer.class.getClassLoader());
        previousType = previousTypeOrdinal != null ? CashFlow.Type.values()[previousTypeOrdinal] : null;
        date = (Long) in.readValue(Long.class.getClassLoader());
        completed = (Boolean) in.readValue(Boolean.class.getClassLoader());
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
        dest.writeParcelable(wallet, flags);
        dest.writeString(categories);
        dest.writeValue(type != null ? type.ordinal() : null);
        dest.writeValue(previousType != null ? previousType.ordinal() : null);
        dest.writeValue(date);
        dest.writeValue(completed);
    }

    public CashFlow buildCashFlow() {
        CashFlow cashFlow = new CashFlow();

        cashFlow.setId(getId());
        cashFlow.setType(getType());
        cashFlow.setAmount(Double.parseDouble(getAmount()));
        cashFlow.setWallet(getWallet());
        for (String tagName : getTags().replaceAll("\\s+", " ").split(" ")) {
            cashFlow.addTag(new Tag(tagName));
        }
        cashFlow.setComment(getComment());
        cashFlow.setDateTime(new Date(getDate()));
        cashFlow.setCompleted(isCompleted());

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
        checkNotNull(type);

        if (this.type == null) {
            this.type = type;
            this.previousType = type;
        } else if (!this.type.equals(type)) {
            this.previousType = this.type;
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

    public String getTags() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
