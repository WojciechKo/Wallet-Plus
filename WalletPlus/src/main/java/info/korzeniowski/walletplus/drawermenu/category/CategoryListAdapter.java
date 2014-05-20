package info.korzeniowski.walletplus.drawermenu.category;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Category;

public class CategoryListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<Category> categoryList;

    CategoryListAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categoryList = categories;
    }

    @Override
    public int getGroupCount() {
        return categoryList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getGroup(groupPosition).getChildren().size();
    }

    @Override
    public Category getGroup(int groupPosition) {
        return categoryList.get(groupPosition);
    }

    @Override
    public Category getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return categoryList.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getChildren().get(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.category_main_list_item, null);
        }
        TextView categoryNameView = (TextView) convertView.findViewById(R.id.text);
        categoryNameView.setText(getGroup(groupPosition).getName());
        categoryNameView.setTypeface(null, Typeface.BOLD);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.category_sub_list_item, null);
        }
        TextView categoryName = (TextView) convertView.findViewById(R.id.text);
        categoryName.setText(getChild(groupPosition, childPosition).getName());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }
}
