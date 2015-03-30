package pl.net.korzeniowski.walletplus.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Comparator;

@DatabaseTable(tableName = Tag.TABLE_NAME)
public class Tag implements Comparable<Tag>, Identifiable, Parcelable {

    public static final String TABLE_NAME = "tag";

    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String COLOR_COLUMN_NAME = "color";

    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    @DatabaseField(columnName = ID_COLUMN_NAME, generatedId = true)
    private Long id;

    @DatabaseField(columnName = NAME_COLUMN_NAME, uniqueIndex = true, canBeNull = false)
    private String name;

    @DatabaseField(columnName = COLOR_COLUMN_NAME, canBeNull = false)
    private Integer color;

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

    public Integer getColor() {
        return color;
    }

    public Tag setColor(Integer color) {
        this.color = color;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        if (id != null ? !id.equals(tag.id) : tag.id != null) return false;
        if (name != null ? !name.equals(tag.name) : tag.name != null) return false;
        if (color != null ? !color.equals(tag.color) : tag.color != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color=" + color +
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
