package info.korzeniowski.walletplus.ui.category;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.widget.IdentityableExpandableListAdapter;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

public class CategoryExpandableListAdapter extends IdentityableExpandableListAdapter<Category> {

    public CategoryExpandableListAdapter(Context context, List<Category> items, OnContentClickListener onContentClickListener) {
        super(context, items, R.layout.category_main_list_item, R.layout.category_sub_list_item, onContentClickListener);
    }

    @Override
    protected MyBaseGroupViewHolder createGroupViewHolder(View convertView) {
        CategoryGroupViewHolder holder = new CategoryGroupViewHolder();
        holder.categoryName = (TextView) convertView.findViewById(R.id.text);
        return holder;
    }

    @Override
    protected void fillGroupViewWithItem(MyBaseGroupViewHolder baseHolder, Category item, boolean isExpanded) {
        CategoryGroupViewHolder holder = (CategoryGroupViewHolder) baseHolder;

        holder.categoryName.setText(item.getName());
        holder.categoryName.setTypeface(null, Typeface.BOLD);
    }

    public class CategoryGroupViewHolder extends MyBaseGroupViewHolder {
        TextView categoryName;
    }

    @Override
    protected MyBaseChildViewHolder createChildHolder(View convertView) {
        CategoryChildViewHolder holder = new CategoryChildViewHolder();
        holder.categoryName = (TextView) convertView.findViewById(R.id.text);
        return holder;
    }

    @Override
    protected void fillChildViewWithItem(MyBaseChildViewHolder baseHolder, Category item) {
        CategoryChildViewHolder holder = (CategoryChildViewHolder) baseHolder;
        holder.categoryName.setText(item.getName());
    }

    class CategoryChildViewHolder extends MyBaseChildViewHolder {
        TextView categoryName;
    }
}
