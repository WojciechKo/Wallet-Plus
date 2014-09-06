package info.korzeniowski.walletplus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.Comparator;
import java.util.List;

public class Category implements Comparable<Category>, Identityable, Childable<Category>, Parcelable {
    public enum Type {
        TRANSFER,
        OTHER,
        INCOME,
        EXPENSE,
        INCOME_EXPENSE;

        public boolean isIncome() {
            return this.equals(INCOME) || this.equals(INCOME_EXPENSE);
        }

        public boolean isExpense() {
            return this.equals(EXPENSE) || this.equals(INCOME_EXPENSE);
        }
    }

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Category parent;

    @DatabaseField(canBeNull = false, uniqueIndex = true)
    private String name;

    @DatabaseField
    private Type type;

    @ForeignCollectionField(orderColumnName = "name")
    private ForeignCollection<Category> children;

    /**
     * ORMLite requirement *
     */
    public Category() {

    }

    public Category(Builder builder) {
        id = builder.id;
        parent = builder.parent;
        name = builder.name;
        type = builder.type;
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
        dest.writeInt(type.ordinal());
    }

    public Long getId() {
        return id;
    }

    public Category getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public final boolean isIncomeType() {
        return getType() != null && getType().isIncome();
    }

    public final boolean isExpenseType() {
        return getType() != null && getType().isExpense();
    }

    @Override
    public List<Category> getChildren() {
        return Lists.newArrayList(children);
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

    @Override
    public final int compareTo(Category other) {
        return Comparators.NAME.compare(this, other);
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

    public static class Builder {
        private Long id;
        private Category parent;
        private String name;
        private Type type;

        public Builder() {

        }

        public Builder(Category category) {
            if (category != null) {
                id = category.getId();
                parent = category.getParent();
                name = category.getName();
                type = category.getType();
            }
        }

        public Long getId() {
            return id;
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Category getParent() {
            return parent;
        }

        public Builder setParent(Category parent) {
            this.parent = parent;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Type getType() {
            return type;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Category build() {
            return new Category(this);
        }
    }
}
