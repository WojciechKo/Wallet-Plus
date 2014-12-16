package info.korzeniowski.walletplus.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;

/**
 * Adapter for items from Main Drawer Menu
 */
public class DrawerMenuAdapter extends BaseAdapter {

    private final MainDrawerContent mainDrawerContent;
    private final WeakReference<Context> context;
    private int selected = -1;

    public DrawerMenuAdapter(Context context, MainDrawerContent mainDrawerContent) {
        this.mainDrawerContent = mainDrawerContent;
        this.context = new WeakReference<>(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context.get()).inflate(R.layout.main_drawer_item, parent, false);
            holder = createHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        fillViewWithItem(holder, position);
        return convertView;
    }

    private ViewHolder createHolder(View convertView) {
        ViewHolder holder = new ViewHolder();

        ButterKnife.inject(holder, convertView);

        convertView.setTag(holder);
        return holder;
    }

    private void fillViewWithItem(ViewHolder holder, int position) {
        MainDrawerItem item = getItem(position);
        holder.menuName.setText(item.getTitle());
        holder.menuName.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), 0, 0, 0);

        if (position == selected) {
            holder.menuName.setTextColor(Color.parseColor("#2e7d32"));
            holder.menuName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.menuName.setTextColor((Color.parseColor("#424242")));
            holder.menuName.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getCount() {
        return mainDrawerContent.getCount();
    }

    @Override
    public MainDrawerItem getItem(int position) {
        return mainDrawerContent.getDrawerItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelected(int position) {
        this.selected = position;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @InjectView(R.id.menu_name)
        TextView menuName;
    }
}
