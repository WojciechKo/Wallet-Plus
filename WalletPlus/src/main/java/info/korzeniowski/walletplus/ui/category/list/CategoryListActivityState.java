package info.korzeniowski.walletplus.ui.category.list;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;

import static info.korzeniowski.walletplus.ui.category.list.CategoryListActivity.Period;

//TODO: Czy Parcelable jest jeszcze używany?
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
    }

    private CategoryListActivityState(Parcel in) {
        long tmpStartDate = in.readLong();
        startDate = tmpStartDate != -1 ? new Date(tmpStartDate) : null;
        period = Period.values()[in.readInt()];
        categoryList = Lists.newArrayList();
        in.readList(categoryList, Category.class.getClassLoader());
    }

    public void clear() {
        startDate = null;
        period = null;
        categoryList = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startDate != null ? startDate.getTime() : -1L);
        dest.writeInt(period.ordinal());
        dest.writeList(categoryList);
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
