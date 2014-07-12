package info.korzeniowski.walletplus.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import info.korzeniowski.walletplus.R;

public class ScrollableCardRelativeLayout extends ScrollView {
    private RelativeLayout content;
    private RelativeLayout.LayoutParams relativeLayoutParams;

    public ScrollableCardRelativeLayout(Context context) {
        super(context);
        postConstruct();
    }

    public ScrollableCardRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        postConstruct();
    }

    public ScrollableCardRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        postConstruct();
    }

    private void postConstruct() {
        content = new RelativeLayout(getContext());
        addView(content);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setLook();
    }

    private void setLook() {
        setCardBackground();
        setCardPadding();
        setContentPadding();
    }

    private void setCardBackground() {
        content.setBackground(getResources().getDrawable(R.drawable.card_background));
    }

    private void setCardPadding() {
        int cardPadding = (int) getResources().getDimension(R.dimen.cardPadding);
        setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
    }

    private void setContentPadding() {
        int contentPadding = (int) getResources().getDimension(R.dimen.cardContentPadding);
        content.setPadding(contentPadding, contentPadding, contentPadding, contentPadding);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        relativeLayoutParams = new RelativeLayout.LayoutParams(getContext(), attrs);
        return super.generateLayoutParams(attrs);
    }

    @Override
    public void addView(@NonNull View child) {
        if (getChildCount() == 0) {
            super.addView(child);
        } else {
            content.addView(child);
        }
    }

    @Override
    public void addView(@NonNull View child, int index) {
        if (getChildCount() == 0) {
            super.addView(child, index);
        } else {
            content.addView(child, index);
        }
    }

    @Override
    public void addView(@NonNull View child, ViewGroup.LayoutParams params) {
        if (getChildCount() == 0) {
            super.addView(child, params);
        } else {
            content.addView(child, relativeLayoutParams);
        }
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() == 0) {
            super.addView(child, index, params);
        } else {
            content.addView(child, index, relativeLayoutParams);
        }
    }
}
