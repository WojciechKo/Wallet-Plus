package info.korzeniowski.walletplus.ui.tag.list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.ui.tag.details.TagDetailsActivity;

import static info.korzeniowski.walletplus.util.KorzeniowskiUtils.Views.dipToPixels;

public class TagListFragment extends Fragment {
    public static final String TAG = TagListFragment.class.getSimpleName();

    @InjectView(R.id.list)
    TextView list;

    @Inject
    @Named("local")
    TagService localTagService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tag_list, container, false);
        ButterKnife.inject(this, view);
        list.setMovementMethod(LinkMovementMethod.getInstance());
        setupList();
        return view;
    }

    private void setupList() {
        List<Tag> tagList = localTagService.getAll();
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (final Tag tag : tagList) {
            builder.append(tag.getName());
            ImageSpan imageSpan = new ImageSpan(createTagDrawable(tag.getName(), tag.getColor()));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent intent = new Intent(getActivity(), TagDetailsActivity.class);
                    intent.putExtra(TagDetailsActivity.EXTRAS_TAG_ID, tag.getId());
                    startActivityForResult(intent, TagDetailsActivity.REQUEST_CODE_EDIT_TAG);
                }
            };
            builder.setSpan(imageSpan, builder.length() - tag.getName().length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(clickableSpan, builder.length() - tag.getName().length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append("  ");
        }
        list.setText(builder);
    }

    private BitmapDrawable createTagDrawable(String tagName, Integer color) {
        //creating textview dynamically
        final TextView tv = new TextView(getActivity());
        tv.setText(tagName);
        tv.setTextSize((float) (getResources().getDimension(R.dimen.xLargeFontSize) * 1.1));
        Drawable drawable = getResources().getDrawable(R.drawable.oval);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC);
        tv.setBackground(drawable);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(dipToPixels(getActivity(), 25), 0, dipToPixels(getActivity(), 25), dipToPixels(getActivity(), 2));

        // Convert View to Drawable
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TagDetailsActivity.REQUEST_CODE_ADD_TAG) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
            }
        } else if (requestCode == TagDetailsActivity.REQUEST_CODE_EDIT_TAG) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
                case TagDetailsActivity.RESULT_DELETED:
                    setupList();
                    return;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
