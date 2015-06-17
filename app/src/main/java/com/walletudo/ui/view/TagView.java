package com.walletudo.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.walletudo.R;

import static com.walletudo.util.WalletudoUtils.Views.dipToPixels;

public class TagView extends TextView {
    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextSize((getResources().getDimension(R.dimen.xxSmallFontSize)));
        setTextColor(Color.WHITE);
        setPadding(dipToPixels(getContext(), 25), 0, dipToPixels(getContext(), 25), dipToPixels(getContext(), 2));
    }

    public void setTagColor(int color) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.oval);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC);
        setBackground(drawable);
    }
}
