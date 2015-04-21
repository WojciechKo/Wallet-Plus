package com.walletudo.ui.tag.list;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.walletudo.R;
import com.walletudo.model.Tag;
import com.walletudo.widget.IdentifiableListAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TagListAdapter extends IdentifiableListAdapter<Tag> {

    public TagListAdapter(Context context, List<Tag> tags) {
        super(context, tags, R.layout.item_tag_list);
    }

    @Override
    protected MyBaseViewHolder createHolder(View convertView) {
        TagViewHolder holder = new TagViewHolder();
        ButterKnife.inject(holder, convertView);
        return holder;
    }

    @Override
    protected void fillViewWithItem(MyBaseViewHolder holder, final Tag item) {
        TagViewHolder tagViewHolder = (TagViewHolder) holder;
        tagViewHolder.tagName.setText(item.getName());
    }

    public class TagViewHolder extends MyBaseViewHolder {
        @InjectView(R.id.tagName)
        TextView tagName;
    }
}
