package com.walletudo.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.walletudo.model.Identifiable;

import java.util.List;

public abstract class IdentifiableListAdapter<T extends Identifiable> extends BaseAdapter {

    private final Context context;
    private final List<T> items;
    private final int itemResourceLayout;

    protected IdentifiableListAdapter(Context context, List<T> items, int itemLayout) {
        this.context = context;
        this.items = items;
        this.itemResourceLayout = itemLayout;
    }

    @Override
    public final int getCount() {
        return items.size();
    }

    @Override
    public final T getItem(int position) {
        return items.get(position);
    }

    @Override
    public final long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        MyBaseViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(itemResourceLayout, null);
            holder = createHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MyBaseViewHolder) convertView.getTag();
        }

        fillViewWithItem(holder, getItem(position));

        return convertView;
    }

    public final void remove(T entity) {
        items.remove(entity);
    }

    protected final Context getContext() {
        return context;
    }

    protected abstract MyBaseViewHolder createHolder(View convertView);

    protected abstract void fillViewWithItem(MyBaseViewHolder holder, T item);

    public static class MyBaseViewHolder {
    }
}

