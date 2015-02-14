package info.korzeniowski.walletplus.ui.tag.list;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.widget.IdentifiableListAdapter;

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
