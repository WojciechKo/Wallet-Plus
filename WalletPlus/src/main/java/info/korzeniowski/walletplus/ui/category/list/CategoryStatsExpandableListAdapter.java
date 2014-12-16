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
import info.korzeniowski.walletplus.widget.OnContentLongClickListener;

public class CategoryStatsExpandableListAdapter extends IdentifiableExpandableListAdapter<Category> {

    private final List<CategoryService.CategoryStats> stats;

    public CategoryStatsExpandableListAdapter(Context context, List<Category> items, List<CategoryService.CategoryStats> stats, OnContentClickListener onContentClickListener, OnContentLongClickListener onContentLongClickListener) {
        super(context, items, R.layout.category_stats_list_item, R.layout.category_stats_list_item, onContentClickListener, onContentLongClickListener);
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
        holder.categoryName.setTextColor(getContext().getResources()
                .getColor(item.getType() == Category.Type.NO_CATEGORY ? R.color.black54A : R.color.black87A));
        holder.categoryName.setText(item.getName());
        holder.categoryName.setTypeface(holder.categoryName.getTypeface(), Typeface.BOLD);
        CategoryService.CategoryStats stats = getStats(item);
        holder.income.setText("Income: " + stats.getTotalIncome() + " (" + stats.getIncome() + ")");
        holder.expense.setText("Expense: " + stats.getTotalExpense() + " (" + stats.getExpense() + ")");
    }

    class CategoryGroupViewHolder extends MyBaseGroupViewHolder {
        @InjectView(R.id.categoryName)
        protected TextView categoryName;

        @InjectView(R.id.income)
        protected TextView income;

        @InjectView(R.id.expense)
        protected TextView expense;
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
        holder.flow.setText("Income: " + stats.getIncome().toString());
        holder.difference.setText("Expense: " + stats.getExpense().toString());
    }

    private CategoryService.CategoryStats getStats(final Category category) {
        return Iterables.find(stats, new Predicate<CategoryService.CategoryStats>() {
            @Override
            public boolean apply(CategoryService.CategoryStats input) {
                if (category.getType() == Category.Type.NO_CATEGORY) {
                    return input.getCategoryId() == null;
                }
                return input.getCategoryId().equals(category.getId());
            }
        });
    }

    class CategoryChildViewHolder extends MyBaseChildViewHolder {
        @InjectView(R.id.categoryName)
        protected TextView categoryName;

        @InjectView(R.id.income)
        protected TextView flow;

        @InjectView(R.id.expense)
        protected TextView difference;
    }
}
