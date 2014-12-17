package info.korzeniowski.walletplus.ui.category.list;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;


public class CategoryListParcelableState implements Parcelable {

    private Date startDate;
    private CategoryListFragmentMain.Period period;
    private List<Category> categoryList;

    public static final Parcelable.Creator<CategoryListParcelableState> CREATOR = new Parcelable.Creator<CategoryListParcelableState>() {
        public CategoryListParcelableState createFromParcel(Parcel in) {
            return new CategoryListParcelableState(in);
        }

        public CategoryListParcelableState[] newArray(int size) {
            return new CategoryListParcelableState[size];
        }
    };

    public CategoryListParcelableState(CategoryListFragmentMain.Period period, List<Category> categoryList) {
        this.startDate = getStartDate(DateTime.now());
        this.period = period;
        this.categoryList = categoryList;
    }

    private Date getStartDate(DateTime dateTime) {
        return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0).toDate();
    }

    private CategoryListParcelableState(Parcel in) {
        startDate = new Date(in.readLong());
        period = CategoryListFragmentMain.Period.values()[in.readInt()];
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

    public CategoryListFragmentMain.Period getPeriod() {
        return period;
    }

    public void setPeriod(CategoryListFragmentMain.Period period) {
        this.period = period;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }
}
