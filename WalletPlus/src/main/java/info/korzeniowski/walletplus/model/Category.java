package info.korzeniowski.walletplus.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.Comparator;

public class Category implements Comparable<Category> {
    public enum Type {INCOME, EXPENSE, INCOME_EXPENSE}

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Category parent;

    @DatabaseField(canBeNull = false, uniqueIndex = true)
    private String name;

    @DatabaseField(canBeNull = false)
    private Type type;

    @ForeignCollectionField
    private ForeignCollection<Category> children;

    public Category() {

    }

    public Category(String name, Type type) {
        this.name = name;
        this.type = type;
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

    public boolean isIncomeType() {
        return Type.INCOME.equals(getType()) || Type.INCOME_EXPENSE.equals(getType());
    }

    public boolean isExpenseType() {
        return Type.EXPENSE.equals(getType()) || Type.INCOME_EXPENSE.equals(getType());
    }

    public ForeignCollection<Category> getChildren() {
        return children;
    }

    @Override
    public int compareTo(Category other) {
        return Comparators.NAME.compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (id != null ? !id.equals(category.id) : category.id != null) return false;
        if (name != null ? !name.equals(category.name) : category.name != null) return false;
        if (parent != null ? !parent.getId().equals(category.parent.getId()) : category.parent != null)
            return false;
        if (type != category.type) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", parent=" + parent +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
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
