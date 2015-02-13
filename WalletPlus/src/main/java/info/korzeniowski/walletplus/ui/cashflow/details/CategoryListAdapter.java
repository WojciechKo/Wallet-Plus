package info.korzeniowski.walletplus.ui.cashflow.details;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.widget.IdentifiableListAdapter;

public class CategoryListAdapter extends IdentifiableListAdapter<Category> {

    public CategoryListAdapter(Context context, List<Category> items) {
        super(context, items, R.layout.item_category_list);
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        CategoryGroupViewHolder holder = new CategoryGroupViewHolder();
        ButterKnife.inject(holder, convertView);
        return holder;
    }

    @Override
    protected void fillViewWithItem(MyBaseViewHolder baseHolder, Category item) {
        CategoryGroupViewHolder holder = (CategoryGroupViewHolder) baseHolder;
        holder.categoryName.setText(item.getName());
        holder.categoryName.setTypeface(null, Typeface.BOLD);
    }

    class CategoryGroupViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.categoryName)
        TextView categoryName;
    }
}
