package com.walletudo.ui.statistics.list;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.walletudo.R;
import com.walletudo.model.Tag;
import com.walletudo.service.StatisticService;
import com.walletudo.widget.IdentifiableListAdapter;

import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class StatisticListAdapter extends IdentifiableListAdapter<Tag> {
    private final List<StatisticService.TagStats> stats;

    public StatisticListAdapter(Context context, List<Tag> items, List<StatisticService.TagStats> stats) {
        super(context, items, R.layout.item_tag_stats_list);
        this.stats = stats;
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        CategoryViewHolder holder = new CategoryViewHolder();
        ButterKnife.inject(holder, convertView);
        holder.incomeIcon.setColorFilter(getContext().getResources().getColor(R.color.green));
        holder.expenseIcon.setColorFilter(getContext().getResources().getColor(R.color.red));
        return holder;
    }

    @Override
    protected void fillViewWithItem(MyBaseViewHolder baseHolder, Tag item) {
        CategoryViewHolder holder = (CategoryViewHolder) baseHolder;
        holder.categoryName.setTextColor(getContext().getResources().getColor(R.color.black87A));
        holder.categoryName.setText(item.getName());
        holder.categoryName.setTypeface(holder.categoryName.getTypeface(), Typeface.BOLD);
        StatisticService.TagStats stats = getStats(item);
        holder.income.setText(NumberFormat.getCurrencyInstance().format(stats.getIncome()));
        holder.expense.setText(NumberFormat.getCurrencyInstance().format(stats.getExpense()));
    }

    private StatisticService.TagStats getStats(final Tag tag) {
        return Iterables.find(stats, new Predicate<StatisticService.TagStats>() {
            @Override
            public boolean apply(StatisticService.TagStats input) {
                return tag.getId().equals(input.getTag().getId());
            }
        });
    }

    class CategoryViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.tagName)
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
