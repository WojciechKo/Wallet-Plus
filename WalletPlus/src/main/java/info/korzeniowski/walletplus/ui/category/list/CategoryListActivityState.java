package info.korzeniowski.walletplus.ui.category.list;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;

import static info.korzeniowski.walletplus.ui.category.list.CategoryListActivity.Period;

public class CategoryListActivityState implements Parcelable {

    public static final Parcelable.Creator<CategoryListActivityState> CREATOR = new Parcelable.Creator<CategoryListActivityState>() {
        public CategoryListActivityState createFromParcel(Parcel in) {
            return new CategoryListActivityState(in);
        }

        public CategoryListActivityState[] newArray(int size) {
            return new CategoryListActivityState[size];
        }
    };

    private Date startDate;
    private Period period;
    private List<Category> categoryList;

    public CategoryListActivityState() {
        this.startDate = getStartDate(DateTime.now());
    }

    private Date getStartDate(DateTime dateTime) {
        return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0).toDate();
    }

    private CategoryListActivityState(Parcel in) {
        startDate = new Date(in.readLong());
        period = Period.values()[in.readInt()];
        categoryList = Arrays.asList((Category[]) in.readParcelableArray(Category.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startDate.getTime());
        dest.writeInt(period.ordinal());
        dest.writeParcelableArray(categoryList.toArray(new Category[categoryList.size()]), flags);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }
}
