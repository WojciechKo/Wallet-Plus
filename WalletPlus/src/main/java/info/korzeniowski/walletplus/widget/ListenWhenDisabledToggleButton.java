package info.korzeniowski.walletplus.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ToggleButton;

public class ListenWhenDisabledToggleButton extends ToggleButton {
    private OnClickWhenDisabledListener disabledListener;

    public ListenWhenDisabledToggleButton(Context context) {
        super(context);
    }

    public ListenWhenDisabledToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ListenWhenDisabledToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnClickWhenDisabledListener(OnClickWhenDisabledListener disabledListener) {
        this.disabledListener = disabledListener;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && !isEnabled()
                && disabledListener != null) {
            disabledListener.onClickWhenDisable();
        }
        return super.onTouchEvent(event);
    }

    public interface OnClickWhenDisabledListener {
        public void onClickWhenDisable();
    }
}
