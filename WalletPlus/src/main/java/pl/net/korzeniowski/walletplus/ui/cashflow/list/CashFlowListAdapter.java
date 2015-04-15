package pl.net.korzeniowski.walletplus.ui.cashflow.list;

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
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.widget.IdentifiableListAdapter;

import static pl.net.korzeniowski.walletplus.util.KorzeniowskiUtils.Views.dipToPixels;

public class CashFlowListAdapter extends IdentifiableListAdapter<CashFlow> {

    public CashFlowListAdapter(Context context, List<CashFlow> casFlows) {
        super(context, casFlows, R.layout.item_cash_flow_list);
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        CashFlowViewHolder holder = new CashFlowViewHolder();
        ButterKnife.inject(holder, convertView);
        return holder;
    }

    @Override
    protected void fillViewWithItem(MyBaseViewHolder baseHolder, CashFlow item) {
        CashFlowViewHolder holder = (CashFlowViewHolder) baseHolder;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (Tag tag : item.getTags()) {
            ImageSpan imageSpan = new ImageSpan(getImageSpanForTag(tag));
            builder.append(tag.getName());
            builder.setSpan(imageSpan, builder.length() - tag.getName().length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ");
        }
        holder.tag.setText(builder);
        holder.wallet.setText(getWalletText(item));
        holder.amount.setText(NumberFormat.getCurrencyInstance().format(item.getAmount()));
        holder.amount.setTextColor(getAmountColor(item));
        holder.date.setText(getDateText(item));
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

    private CharSequence getWalletText(CashFlow item) {
        return item.getWallet().getName();
    }

    private int getAmountColor(CashFlow item) {
        CashFlow.Type type = item.getType();

        if (type == CashFlow.Type.EXPANSE) {
            return getContext().getResources().getColor(R.color.red);
        } else if (type == CashFlow.Type.INCOME) {
            return getContext().getResources().getColor(R.color.green);
        } else if (type == CashFlow.Type.TRANSFER) {
            return getContext().getResources().getColor(R.color.blue);
        }
        return getContext().getResources().getColor(R.color.black);
    }

    private String getDateText(CashFlow item) {
        String timeString = DateFormat.getTimeFormat(getContext()).format(item.getDateTime());
        String dateString = DateFormat.getDateFormat(getContext()).format(item.getDateTime());
        return dateString + " " + timeString;
    }

    private CharSequence getLabeledSpannable(String label, String text) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(label + " " + text);
        spanTxt.setSpan(new AbsoluteSizeSpan((int) getContext().getResources().getDimension(R.dimen.mediumFontSize)), spanTxt.length() - text.length(), spanTxt.length(), 0);
        return spanTxt;
    }

    class CashFlowViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.wallet)
        TextView wallet;

        @InjectView(R.id.amount)
        TextView amount;

        @InjectView(R.id.tag)
        TextView tag;

        @InjectView(R.id.date)
        TextView date;
    }
}
