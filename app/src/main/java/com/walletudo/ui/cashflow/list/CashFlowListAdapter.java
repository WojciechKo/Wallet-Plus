package com.walletudo.ui.cashflow.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.walletudo.R;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Tag;

import org.joda.time.LocalDate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.walletudo.util.WalletudoUtils.Views.dipToPixels;

public class CashFlowListAdapter extends BaseExpandableListAdapter {
    private final ArrayList<ArrayList<CashFlow>> children;
    private final ArrayList<LocalDate> groups;
    private Context context;

    public CashFlowListAdapter(Context context, List<CashFlow> cashflows) {
        this.context = context;
        this.groups = Lists.newArrayList();
        this.children = Lists.newArrayList();

        for (final CashFlow cashFlow : cashflows) {
            LocalDate groupOfCashFlow = new LocalDate(cashFlow.getDateTime());

            ArrayList<CashFlow> childList;
            int groupIndex = groups.indexOf(groupOfCashFlow);
            if (groupIndex == -1) {
                groups.add(groupOfCashFlow);
                childList = Lists.newArrayList();
                children.add(childList);
            } else {
                childList = this.children.get(groupIndex);
            }
            childList.add(cashFlow);
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public LocalDate getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group_cash_flow_list, parent, false);
            convertView.setTag(new GroupCashFlowViewHolder(convertView));
        }
        GroupCashFlowViewHolder holder = (GroupCashFlowViewHolder) convertView.getTag();
        LocalDate group = getGroup(groupPosition);

        holder.divider.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.label.setText(DateFormat.getDateFormat(context).format(group.toDate()));
        return convertView;
    }

    class GroupCashFlowViewHolder {
        @InjectView(R.id.divider)
        View divider;

        @InjectView(R.id.label)
        TextView label;

        public GroupCashFlowViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public CashFlow getChild(int groupPosition, int childPosition) {
        return children.get(groupPosition).get(childPosition);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return children.get(groupPosition).size();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).getId();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_child_cash_flow_list, parent, false);
            convertView.setTag(new ChildCashFlowViewHolder(convertView));
        }
        ChildCashFlowViewHolder holder = (ChildCashFlowViewHolder) convertView.getTag();
        CashFlow child = getChild(groupPosition, childPosition);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (Tag tag : child.getTags()) {
            ImageSpan imageSpan = new ImageSpan(getImageSpanForTag(tag));
            builder.append(tag.getName());
            builder.setSpan(imageSpan, builder.length() - tag.getName().length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ");
        }
        holder.tag.setText(builder);
        holder.wallet.setText(getWalletText(child));
        holder.amount.setText(NumberFormat.getCurrencyInstance().format(child.getAmount()));
        holder.amount.setTextColor(getAmountColor(child));
        if (Strings.isNullOrEmpty(child.getComment())) {
            holder.comment.setVisibility(View.GONE);
        } else {
            holder.comment.setVisibility(View.VISIBLE);
            holder.comment.setText(child.getComment());
        }
        holder.date.setText(getDateText(child));

        return convertView;
    }

    private int getAmountColor(CashFlow item) {
        CashFlow.Type type = item.getType();

        if (type == CashFlow.Type.EXPENSE) {
            return context.getResources().getColor(R.color.red);
        } else if (type == CashFlow.Type.INCOME) {
            return context.getResources().getColor(R.color.green);
        } else if (type == CashFlow.Type.TRANSFER) {
            return context.getResources().getColor(R.color.blue);
        }
        return context.getResources().getColor(R.color.black);
    }

    private String getDateText(CashFlow item) {
        String timeString = DateFormat.getTimeFormat(context).format(item.getDateTime());
//        String dateString = DateFormat.getDateFormat(context).format(item.getDateTime());
        return timeString;
    }

    private BitmapDrawable getImageSpanForTag(Tag tag) {
        // creating textview dynamically
        final TextView tv = new TextView(context);
        tv.setText(tag.getName());
        tv.setTextSize(context.getResources().getDimension(R.dimen.mediumFontSize));
        Drawable drawable = context.getResources().getDrawable(R.drawable.oval);
        drawable.setColorFilter(tag.getColor(), PorterDuff.Mode.SRC);
        tv.setBackground(drawable);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(dipToPixels(context, 15), 0, dipToPixels(context, 15), dipToPixels(context, 1));

        // convert View to Drawable
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv.measure(spec, spec);
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(tv.getMeasuredWidth(), tv.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-tv.getScrollX(), -tv.getScrollY());
        tv.draw(c);
        tv.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = tv.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        tv.destroyDrawingCache();

        BitmapDrawable bitmapDrawable = new BitmapDrawable(viewBmp);
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());

        return bitmapDrawable;
    }

    private CharSequence getWalletText(CashFlow item) {
        return item.getWallet().getName();
    }

    class ChildCashFlowViewHolder {
        @InjectView(R.id.wallet)
        TextView wallet;

        @InjectView(R.id.amount)
        TextView amount;

        @InjectView(R.id.comment)
        TextView comment;

        @InjectView(R.id.tag)
        TextView tag;

        @InjectView(R.id.date)
        TextView date;

        public ChildCashFlowViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
