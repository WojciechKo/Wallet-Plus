package com.walletudo.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Date;

public class PeriodView extends TextView {
    public PeriodView(Context context) {
        super(context);
        init();
    }

    public PeriodView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PeriodView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PeriodView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setPeriod(Date from, Date to) {
        setText(DateUtils.formatDateRange(getContext(), from.getTime(), to.getTime(), 0));
    }
}
