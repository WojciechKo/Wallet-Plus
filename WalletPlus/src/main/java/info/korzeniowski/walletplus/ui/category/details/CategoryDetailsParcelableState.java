package info.korzeniowski.walletplus.ui.category.details;

import android.os.Parcel;
import android.os.Parcelable;

import info.korzeniowski.walletplus.model.Category;

public class CategoryDetailsParcelableState implements Parcelable {
    private Long id;
    private Long parentId;
    private String name;
    private Category.Type type;

    public static final Parcelable.Creator<CategoryDetailsParcelableState> CREATOR = new Parcelable.Creator<CategoryDetailsParcelableState>() {
        public CategoryDetailsParcelableState createFromParcel(Parcel in) {
            return new CategoryDetailsParcelableState(in);
        }

        public CategoryDetailsParcelableState[] newArray(int size) {
            return new CategoryDetailsParcelableState[size];
        }
    };

    public CategoryDetailsParcelableState(Parcel in) {
        id = in.readLong();
        parentId = in.readLong();
        name = in.readString();
        type = Category.Type.values()[in.readInt()];
    }

    public CategoryDetailsParcelableState(Category category) {
        if (category != null) {
            id = category.getId();
            parentId = category.getParent() != null ? category.getParent().getId() : null;
            name = category.getName();
            type = category.getType();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(parentId);
        dest.writeString(name);
        dest.writeInt(type.ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category.Type getType() {
        return type;
    }

    public void setType(Category.Type type) {
        this.type = type;
    }
}
