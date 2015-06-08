package com.walletudo.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.walletudo.R;

import java.text.NumberFormat;

public class AmountView extends TextView {
    public AmountView(Context context) {
        super(context);
        init();
    }

    public AmountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AmountView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setAmount(Double amount) {
        setText(NumberFormat.getCurrencyInstance().format(amount));

        if (1 / amount > 0) {
            setTextColor(getResources().getColor(R.color.green));
        } else {
            setTextColor(getResources().getColor(R.color.red));
        }
    }
}
