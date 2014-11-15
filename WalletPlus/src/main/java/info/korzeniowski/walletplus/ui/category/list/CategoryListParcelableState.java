package info.korzeniowski.walletplus.ui.category.list;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.util.Date;


public class CategoryListParcelableState implements Parcelable {

    private Date startDate;
    private CategoryListFragmentMain.Period period;

    public static final Parcelable.Creator<CategoryListParcelableState> CREATOR = new Parcelable.Creator<CategoryListParcelableState>() {
        public CategoryListParcelableState createFromParcel(Parcel in) {
            return new CategoryListParcelableState(in);
        }

        public CategoryListParcelableState[] newArray(int size) {
            return new CategoryListParcelableState[size];
        }
    };

    public CategoryListParcelableState(CategoryListFragmentMain.Period period) {
        this.startDate = getStartDate(DateTime.now());
        this.period = period;
    }

    private Date getStartDate(DateTime dateTime) {
        return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0).toDate();
    }

    public CategoryListParcelableState(Parcel in) {
        startDate = new Date(in.readLong());
        period = CategoryListFragmentMain.Period.values()[in.readInt()];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startDate.getTime());
        dest.writeInt(period.ordinal());
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public CategoryListFragmentMain.Period getPeriod() {
        return period;
    }

    public void setPeriod(CategoryListFragmentMain.Period period) {
        this.period = period;
    }
}
