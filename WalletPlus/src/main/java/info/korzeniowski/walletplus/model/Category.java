package info.korzeniowski.walletplus.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Category implements Comparable<Category>, Identityable, Childable<Category>, Parcelable {
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Category parent;

    @DatabaseField
    private String name;

    @DatabaseField
    private Type type;

    @ForeignCollectionField(orderColumnName = "name")
    private ForeignCollection<Category> children;

    public Category() {

    }

    public Category(Category category) {
        checkNotNull(category);
        setId(category.getId());
        setParent(category.getParent() == null ? null : new Category(category.getParent()));
        setName(category.getName());
        setType(category.getType());
    }

    private Category(Parcel in) {
        id = in.readLong();
        parent = in.readParcelable(Category.class.getClassLoader());
        name = in.readString();
        type = (Type) in.readValue(Type.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(parent, flags);
        dest.writeString(name);
        dest.writeValue(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category category = (Category) o;

        if (id != null ? !id.equals(category.id) : category.id != null) return false;
        if (name != null ? !name.equals(category.name) : category.name != null) return false;
        if (parent != null ? !parent.getId().equals(category.parent.getId()) : category.parent != null)
            return false;
        if (type != category.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public final String toString() {
        return "Category{" +
                "id=" + id +
                ", parent=" + parent +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    public Long getId() {
        return id;
    }

    public Category setId(Long id) {
        this.id = id;
        return this;
    }

    public Category getParent() {
        return parent;
    }

    public Category setParent(Category parent) {
        this.parent = parent;
        return this;
    }

    public String getName() {
        return name;
    }

    public Category setName(String name) {
        this.name = name;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Category setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public List<Category> getChildren() {
        if (children == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(children);
    }

    @Override
    public final int compareTo(@NonNull Category other) {
        return Comparators.NAME.compare(this, other);
    }

    public enum Type {
        TRANSFER,
        NO_CATEGORY
    }

    public static class Comparators {
        public static final Comparator<Category> NAME = new Comparator<Category>() {
            @Override
            public int compare(Category category1, Category category2) {
                String categoryName1 = category1.getName().toUpperCase();
                String categoryName2 = category2.getName().toUpperCase();
                return categoryName1.compareTo(categoryName2);
            }
        };
    }
}
