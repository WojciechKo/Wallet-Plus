package info.korzeniowski.walletplus.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkNotNull;

public class Tag implements Comparable<Tag>, Identifiable, Parcelable {
    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    public final static String ID_FIELD_NAME = "id";
    public final static String NAME_FIELD_NAME = "name";

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(uniqueIndex = true, canBeNull = false)
    private String name;

    public Tag() {

    }

    public Tag(String name) {
        this.name = name;
    }

    private Tag(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }

    public Long getId() {
        return id;
    }

    public Tag setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Tag setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Tag tag = (Tag) o;

        if (id != null ? !id.equals(tag.id) : tag.id != null)
            return false;
        if (name != null ? !name.equals(tag.name) : tag.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public final String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public final int compareTo(@NonNull Tag other) {
        return Comparators.NAME.compare(this, other);
    }

    public static class Comparators {
        public static final Comparator<Tag> NAME = new Comparator<Tag>() {
            @Override
            public int compare(Tag tag1, Tag tag2) {
                String name1 = tag1.getName().toUpperCase();
                String name2 = tag2.getName().toUpperCase();
                return name1.compareTo(name2);
            }
        };
    }
}
