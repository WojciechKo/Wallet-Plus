package info.korzeniowski.walletplus.ui.category.list;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.widget.IdentifiableExpandableListAdapter;
import info.korzeniowski.walletplus.widget.OnContentClickListener;

public class CategoryStatsExpandableListAdapter extends IdentifiableExpandableListAdapter<Category> {

    private final List<CategoryService.CategoryStats> stats;

    public CategoryStatsExpandableListAdapter(Context context, List<Category> items, List<CategoryService.CategoryStats> stats, OnContentClickListener onContentClickListener) {
        super(context, items, R.layout.category_stats_list_item, R.layout.category_stats_list_item, onContentClickListener);
        this.stats = stats;
    }

    @Override
    protected MyBaseGroupViewHolder createGroupViewHolder(View convertView) {
        CategoryGroupViewHolder holder = new CategoryGroupViewHolder();
        ButterKnife.inject(holder, convertView);
        return holder;
    }

    @Override
    protected void fillGroupViewWithItem(MyBaseGroupViewHolder baseHolder, Category item, boolean isExpanded) {
        CategoryGroupViewHolder holder = (CategoryGroupViewHolder) baseHolder;
        holder.categoryName.setTextColor(getContext().getResources().getColor(R.color.black87A));
        holder.categoryName.setText(item.getName());
        holder.categoryName.setTypeface(holder.categoryName.getTypeface(), Typeface.BOLD);
        CategoryService.CategoryStats stats = getStats(item);
        holder.flow.setText("Total flow: " + stats.getTotalFlow() + " (" + stats.getFlow() + ")");
        holder.difference.setText("Total diff: " + stats.getTotalDifference() + " (" + stats.getDifference() + ")");
    }

    class CategoryGroupViewHolder extends MyBaseGroupViewHolder {
        @InjectView(R.id.categoryName)
        protected TextView categoryName;

        @InjectView(R.id.flow)
        protected TextView flow;

        @InjectView(R.id.difference)
        protected TextView difference;
    }

    @Override
    protected MyBaseChildViewHolder createChildHolder(View convertView) {
        CategoryChildViewHolder holder = new CategoryChildViewHolder();
        ButterKnife.inject(holder, convertView);
        return holder;
    }

    @Override
    protected void fillChildViewWithItem(MyBaseChildViewHolder baseHolder, Category item) {
        CategoryChildViewHolder holder = (CategoryChildViewHolder) baseHolder;
        holder.categoryName.setTextColor(getContext().getResources().getColor(R.color.black54A));
        holder.categoryName.setText(item.getName());
        CategoryService.CategoryStats stats = getStats(item);
        holder.flow.setText("Flow: " + stats.getFlow().toString());
        holder.difference.setText("Diff:" + stats.getDifference().toString());
    }

    private CategoryService.CategoryStats getStats(final Category category) {
        return Iterables.find(stats, new Predicate<CategoryService.CategoryStats>() {
            @Override
            public boolean apply(CategoryService.CategoryStats input) {
                return input.getCategoryId().equals(category.getId());
            }
        });
    }

    class CategoryChildViewHolder extends MyBaseChildViewHolder {
        @InjectView(R.id.categoryName)
        protected TextView categoryName;

        @InjectView(R.id.flow)
        protected TextView flow;

        @InjectView(R.id.difference)
        protected TextView difference;
    }
}
