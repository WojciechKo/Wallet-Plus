package pl.net.korzeniowski.walletplus.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;

import info.hoang8f.widget.FButton;

public class CheckableFButton extends FButton implements Checkable {
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private boolean checked = false;

    public CheckableFButton(Context context) {
        super(context);
    }

    public CheckableFButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableFButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (checked) {
            mergeDrawableStates(drawableState, STATE_CHECKED);
        }
        return drawableState;
    }
}
