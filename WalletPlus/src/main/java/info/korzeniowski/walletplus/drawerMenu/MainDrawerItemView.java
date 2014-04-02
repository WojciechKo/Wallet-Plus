package info.korzeniowski.walletplus.drawermenu;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

import javax.inject.Inject;

import info.korzeniowski.walletplus.R;

/**
 * View class for Main Drawer Item.
 */
@EViewGroup(R.layout.main_drawer_item)
public class MainDrawerItemView extends LinearLayout{

    @ViewById
    TextView itemName;

    @ViewById
    ImageView itemIcon;

    @Inject
    public MainDrawerItemView(Context context) {
        super(context);
        setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public void bind(MainDrawerItem item) {
        itemName.setText(item.getTitle());
        itemIcon.setImageResource(item.getIcon());
    }
}
