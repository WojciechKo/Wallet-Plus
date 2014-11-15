package info.korzeniowski.walletplus.ui.category.list;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.util.Date;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;


public class CategoryListParcelableState implements Parcelable{

    private Date fromDate;
    private CategoryListFragmentMain.Period period;

    public static final Parcelable.Creator<CategoryListParcelableState> CREATOR = new Parcelable.Creator<CategoryListParcelableState>() {
        public CategoryListParcelableState createFromParcel(Parcel in) {
            return new CategoryListParcelableState(in);
        }

        public CategoryListParcelableState[] newArray(int size) {
            return new CategoryListParcelableState[size];
        }
    };

    public CategoryListParcelableState() {
        fromDate = DateTime.now().toDate();
        period = CategoryListFragmentMain.Period.WEEK;
    }

    public CategoryListParcelableState(Parcel in) {
        fromDate = new Date(in.readLong());
        period = CategoryListFragmentMain.Period.values()[in.readInt()];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(fromDate.getTime());
        dest.writeInt(period.ordinal());
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public CategoryListFragmentMain.Period getPeriod() {
        return period;
    }

    public void setPeriod(CategoryListFragmentMain.Period period) {
        this.period = period;
    }
}
