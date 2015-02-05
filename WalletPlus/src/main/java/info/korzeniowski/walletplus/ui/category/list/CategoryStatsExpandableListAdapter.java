package info.korzeniowski.walletplus.ui.category.list;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.text.NumberFormat;
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
        super(context, items, R.layout.item_category_stats_list, R.layout.item_category_stats_list, onContentClickListener, onContentLongClickListener);
        this.stats = stats;
    }

    @Override
    protected MyBaseGroupViewHolder createGroupViewHolder(View convertView) {
        CategoryGroupViewHolder holder = new CategoryGroupViewHolder();
        ButterKnife.inject(holder, convertView);
        holder.incomeIcon.setColorFilter(getContext().getResources().getColor(R.color.green));
        holder.expenseIcon.setColorFilter(getContext().getResources().getColor(R.color.red));
        return holder;
    }

    @Override
    protected void fillGroupViewWithItem(MyBaseGroupViewHolder baseHolder, Category item, boolean isExpanded) {
        CategoryGroupViewHolder holder = (CategoryGroupViewHolder) baseHolder;
        holder.categoryName.setTextColor(getContext().getResources()
                .getColor(item.getId().equals(CategoryService.CATEGORY_NULL_ID) ? R.color.black54A : R.color.black87A));
        holder.categoryName.setText(item.getName());
        holder.categoryName.setTypeface(holder.categoryName.getTypeface(), Typeface.BOLD);
        CategoryService.CategoryStats stats = getStats(item);
        holder.income.setText(NumberFormat.getCurrencyInstance().format(stats.getIncome()));
        holder.expense.setText(NumberFormat.getCurrencyInstance().format(stats.getExpense()));
    }

    class CategoryGroupViewHolder extends MyBaseGroupViewHolder {
        @InjectView(R.id.categoryName)
        TextView categoryName;

        @InjectView(R.id.incomeIcon)
        ImageView incomeIcon;

        @InjectView(R.id.income)
        TextView income;

        @InjectView(R.id.expenseIcon)
        ImageView expenseIcon;

        @InjectView(R.id.expense)
        TextView expense;
    }

    @Override
    protected MyBaseChildViewHolder createChildHolder(View convertView) {
        CategoryChildViewHolder holder = new CategoryChildViewHolder();
        ButterKnife.inject(holder, convertView);
        holder.incomeIcon.setColorFilter(getContext().getResources().getColor(R.color.green));
        holder.expenseIcon.setColorFilter(getContext().getResources().getColor(R.color.red));
        return holder;
    }

    @Override
    protected void fillChildViewWithItem(MyBaseChildViewHolder baseHolder, Category item) {
        CategoryChildViewHolder holder = (CategoryChildViewHolder) baseHolder;
        holder.categoryName.setTextColor(getContext().getResources().getColor(R.color.black54A));
        holder.categoryName.setText(item.getName());
        CategoryService.CategoryStats stats = getStats(item);
        holder.income.setText(NumberFormat.getCurrencyInstance().format(stats.getIncome()));
        holder.expense.setText(NumberFormat.getCurrencyInstance().format(stats.getExpense()));
    }

    private CategoryService.CategoryStats getStats(final Category category) {
        return Iterables.find(stats, new Predicate<CategoryService.CategoryStats>() {
            @Override
            public boolean apply(CategoryService.CategoryStats input) {
                return category.getId().equals(input.getCategoryId());
            }
        });
    }

    class CategoryChildViewHolder extends MyBaseChildViewHolder {
        @InjectView(R.id.categoryName)
        TextView categoryName;

        @InjectView(R.id.incomeIcon)
        ImageView incomeIcon;

        @InjectView(R.id.income)
        TextView income;

        @InjectView(R.id.expenseIcon)
        ImageView expenseIcon;

        @InjectView(R.id.expense)
        TextView expense;
    }
}
