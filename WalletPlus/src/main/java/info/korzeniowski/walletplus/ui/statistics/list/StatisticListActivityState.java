package info.korzeniowski.walletplus.ui.statistics.list;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

import info.korzeniowski.walletplus.model.Tag;

import static info.korzeniowski.walletplus.ui.statistics.list.StatisticListActivity.Period;

//TODO: Czy Parcelable jest jeszcze używany?
public class StatisticListActivityState implements Parcelable {

    public static final Parcelable.Creator<StatisticListActivityState> CREATOR = new Parcelable.Creator<StatisticListActivityState>() {
        public StatisticListActivityState createFromParcel(Parcel in) {
            return new StatisticListActivityState(in);
        }

        public StatisticListActivityState[] newArray(int size) {
            return new StatisticListActivityState[size];
        }
    };

    private Date startDate;
    private Period period;
    private List<Tag> tagList;

    public StatisticListActivityState() {
    }

    private StatisticListActivityState(Parcel in) {
        long tmpStartDate = in.readLong();
        startDate = tmpStartDate != -1 ? new Date(tmpStartDate) : null;
        period = Period.values()[in.readInt()];
        tagList = Lists.newArrayList();
        in.readList(tagList, Tag.class.getClassLoader());
    }

    public void clear() {
        startDate = null;
        period = null;
        tagList = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startDate != null ? startDate.getTime() : -1L);
        dest.writeInt(period.ordinal());
        dest.writeList(tagList);
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

    public List<Tag> getTagList() {
        return tagList;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }
}
