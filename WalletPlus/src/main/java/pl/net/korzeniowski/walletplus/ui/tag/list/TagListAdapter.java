package pl.net.korzeniowski.walletplus.ui.tag.list;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.widget.IdentifiableListAdapter;

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
