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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.walletudo.R;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Tag;
import com.walletudo.model.Wallet;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.walletudo.util.WalletudoUtils.Views.dipToPixels;

public class CashFlowListItemView extends FrameLayout {
    @InjectView(R.id.date)
    TextView date;

    @InjectView(R.id.amount)
    TextView amount;

    @InjectView(R.id.wallet)
    TextView wallet;

    @InjectView(R.id.tag)
    TextView tag;

    @InjectView(R.id.comment)
    TextView comment;

    public CashFlowListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.item_child_cash_flow_list, this);
        ButterKnife.inject(this);
    }

    public void setCashFlow(CashFlow cashFlow) {
        setTags(cashFlow.getTags());
        setWallet(cashFlow.getWallet());
        setAmount(cashFlow.getAmount(), cashFlow.getType());
        setComment(cashFlow.getComment());
        setDate(cashFlow.getDateTime());
    }

    public void setTags(List<Tag> tags) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (Tag tag : tags) {
            ImageSpan imageSpan = new ImageSpan(getImageSpanForTag(tag));
            builder.append(tag.getName());
            builder.setSpan(imageSpan, builder.length() - tag.getName().length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ");
        }
        tag.setText(builder);
    }

    private BitmapDrawable getImageSpanForTag(Tag tag) {
        // creating textview dynamically
        final TextView tv = new TextView(getContext());
        tv.setText(tag.getName());
        tv.setTextSize(getContext().getResources().getDimension(R.dimen.mediumFontSize));
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.oval);
        drawable.setColorFilter(tag.getColor(), PorterDuff.Mode.SRC);
        tv.setBackground(drawable);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(dipToPixels(getContext(), 15), 0, dipToPixels(getContext(), 15), dipToPixels(getContext(), 1));

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

    public void setDate(Date date) {
        this.date.setText(getDateText(date));
    }

    private String getDateText(Date date) {
        String timeString = DateFormat.getTimeFormat(getContext()).format(date);
        String dateString = DateFormat.getDateFormat(getContext()).format(date);
        return dateString + " " + timeString;
    }

    public void setAmount(Double amount, CashFlow.Type cashFlowType) {
        this.amount.setText(NumberFormat.getCurrencyInstance().format(amount));
        this.amount.setTextColor(getAmountColor(cashFlowType));
    }

    private int getAmountColor(CashFlow.Type cashFlowType) {
        if (cashFlowType == CashFlow.Type.EXPENSE) {
            return getContext().getResources().getColor(R.color.red);
        } else if (cashFlowType == CashFlow.Type.INCOME) {
            return getContext().getResources().getColor(R.color.green);
        } else if (cashFlowType == CashFlow.Type.TRANSFER) {
            return getContext().getResources().getColor(R.color.blue);
        }
        return getContext().getResources().getColor(R.color.black);
    }

    public void setWallet(Wallet wallet) {
        this.wallet.setText(wallet.getName());
    }


    public void setComment(String comment) {
        if (Strings.isNullOrEmpty(comment)) {
            this.comment.setVisibility(View.GONE);
        } else {
            this.comment.setVisibility(View.VISIBLE);
            this.comment.setText(comment);
        }
    }
}
