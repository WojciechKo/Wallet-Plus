package info.korzeniowski.walletplus.model;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Wojtek on 27.03.14.
 */
public class Category {
    public static Long INVALID_ID = (long) -1;

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
        this.id = id != null ? id : INVALID_ID;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public Category setParentId(Long parentId) {
        this.parentId = parentId != null ? parentId : INVALID_ID;
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Category) {
            Category other = (Category) o;
            if (ObjectUtils.equals(other.getId(), getId()) &&
                    ObjectUtils.equals(other.getName(), getName()) &&
                    ObjectUtils.equals(other.getParentId(), getParentId()) &&
                    other.getTypes().equals(getTypes()) ) {
                return true;
            }
            return true;
        }

        return false;
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
