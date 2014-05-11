package info.korzeniowski.walletplus.model;

import com.google.common.base.Objects;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
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
        types = EnumSet.allOf(Type.class);
        children = new ArrayList<Category>();
    }

    public Category(String name, EnumSet<Type> types) {
        this.name = name;
        this.types = types;
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

    public Category setTypes(EnumSet<Type> types) {
        this.types = types != null ? types : EnumSet.noneOf(Category.Type.class);
        return this;
    }

    public Category setType(Type type) {
        return type != null ? setTypes(EnumSet.of(type)) : setTypes(EnumSet.noneOf(Category.Type.class));
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Category) {
            Category other = (Category) o;
            if (Objects.equal(other.getId(), getId()) &&
                    Objects.equal(other.getName(), getName()) &&
                    Objects.equal(other.getParentId(), getParentId()) &&
                    other.getTypes().equals(getTypes()) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Category other) {
        String thisName = getName().toUpperCase();
        String otherName = other.getName().toUpperCase();
        return thisName.compareTo(otherName);
    }

    public static class Comparators {

        public static Comparator<Category> NAME = new Comparator<Category>() {
            @Override
            public int compare(Category category1, Category category2) {
                String categoryName1 = category1.getName().toUpperCase();
                String categoryName2 = category2.getName().toUpperCase();
                return categoryName1.compareTo(categoryName2);
            }
        };

        public static Comparator<Category> POSITION = new Comparator<Category>() {

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
