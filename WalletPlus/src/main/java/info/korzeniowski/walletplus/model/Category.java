package info.korzeniowski.walletplus.model;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.EnumUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class Category implements Comparable<Category> {
    private Long id;
    private Long parentId;
    private Category parent;
    private String name;
    private EnumSet<Type> types;
    private List<Category> children;

    public Category() {
        types = EnumSet.noneOf(Type.class);
        children = Lists.newArrayList();
    }

    public Long getId() {
        return id;
    }

    public Category setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public Category setParentId(Long parentId) {
        this.parentId = parentId;
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

    public EnumSet<Type> getTypes() {
        return types;
    }

    public Category setTypes(Type type) {
        return type != null ? setTypes(EnumSet.of(type)) : setTypes(EnumSet.noneOf(Category.Type.class));
    }

    public Category setTypes(EnumSet<Type> types) {
        this.types = types != null ? types : EnumSet.noneOf(Category.Type.class);
        return this;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public void addChild(Category category) {
        children.add(category);
        Collections.sort(children, Comparators.NAME);
    }

    public boolean removeChild(Category category) {
        return children.remove(category);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Category) {
            Category other = (Category) object;
            return Objects.equal(other.getId(), getId()) &&
                    Objects.equal(other.getName(), getName()) &&
                    Objects.equal(other.getParentId(), getParentId()) &&
                    other.getTypes().equals(getTypes());
        }
        return false;
    }

    @Override
    public int compareTo(Category other) {
        //TODO: uwzględnić bycie MainCategory.
        String thisName = getName().toUpperCase();
        String otherName = other.getName().toUpperCase();
        return thisName.compareTo(otherName);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", types=" + types +
                '}';
    }

    public static Category findById(List<Category> categories, final Long id) {
        return Iterables.tryFind(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category category) {
                return Objects.equal(category.getId(), id);
            }
        }).orNull();
    }

    public static Category findByName(final List<Category> categories, final String name) {
        return Iterables.tryFind(categories, new Predicate<Category>() {
            @Override
            public boolean apply(Category category) {
                return Objects.equal(category.getName(), name);
            }
        }).orNull();
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

        public static final Comparator<Category> POSITION = new Comparator<Category>() {

            @Override
            public int compare(Category category1, Category category2) {
                //TODO: napisać
                return category1.compareTo(category2);
            }
        };
    }

    public enum Type {
        INCOME,
        EXPENSE;

        public static EnumSet<Type> convertBitwiseToEnumSet(int bitwise) {
            return EnumUtils.processBitVector(Type.class, bitwise);
        }

        public static int convertEnumToBitwise(EnumSet<Type> enumSet) {
            return (int) EnumUtils.generateBitVector(Type.class, enumSet);
        }

        public static int convertEnumToBitwise(Type type) {
            return (int) EnumUtils.generateBitVector(Type.class,type);
        }
    }
}
