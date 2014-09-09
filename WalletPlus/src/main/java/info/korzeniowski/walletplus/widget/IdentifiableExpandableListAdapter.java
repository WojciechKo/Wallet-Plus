package info.korzeniowski.walletplus.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Childable;
import info.korzeniowski.walletplus.model.Identityable;

public abstract class IdentifiableExpandableListAdapter<T extends Identityable & Childable<T>> extends BaseExpandableListAdapter {
    private final Context context;
    private final List<T> items;
    private final int groupItemLayout;
    private final int childItemLayout;
    OnContentClickListener listener;

    public IdentifiableExpandableListAdapter(Context context, List<T> items, int groupItemLayout, int childItemLayout, OnContentClickListener listener) {
        this.context = context;
        this.items = items;
        this.groupItemLayout = groupItemLayout;
        this.childItemLayout = childItemLayout;
        this.listener = listener;
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getGroup(groupPosition).getChildren().size();
    }

    @Override
    public T getGroup(int groupPosition) {
        return items.get(groupPosition);
    }

    @Override
    public T getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getGroup(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public final View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        GroupViewHolder groupViewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expandable_group_content, null);
            groupViewHolder = getGroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }

        setupContent(groupViewHolder, groupPosition);

        if (getChildrenCount(groupPosition) > 0) {
            setupIndicatorAsExpandable(groupViewHolder, (ExpandableListView) parent, groupPosition);
        } else {
            setupIndicatorAsNotExpandable(groupViewHolder, groupPosition);
        }

        fillGroupViewWithItem(groupViewHolder.contentViewHolder, getGroup(groupPosition), isExpanded);
        return convertView;
    }

    private void setupContent(GroupViewHolder groupViewHolder, final int groupPosition) {
        groupViewHolder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onContentClick(getGroupId(groupPosition));
            }
        });
    }

    private void setupIndicatorAsExpandable(GroupViewHolder groupViewHolder, final ExpandableListView listView, final int groupPosition) {
        groupViewHolder.groupIndicator.setImageResource(listView.isGroupExpanded(groupPosition) ? R.drawable.arrow_down : R.drawable.arrow_left);
        groupViewHolder.groupIndicatorFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroup(groupPosition);
                } else {
                    listView.expandGroup(groupPosition);
                }
            }
        });
    }

    private void setupIndicatorAsNotExpandable(GroupViewHolder groupViewHolder, final int groupPosition) {
        groupViewHolder.groupIndicator.setImageDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
        groupViewHolder.groupIndicatorFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onContentClick(getGroupId(groupPosition));
            }
        });
    }

    private GroupViewHolder getGroupViewHolder(View convertView) {
        GroupViewHolder groupViewHolder = new GroupViewHolder();
        groupViewHolder.groupIndicatorFrame = (FrameLayout) convertView.findViewById(R.id.group_indicator_frame);
        groupViewHolder.groupIndicator = (ImageView) convertView.findViewById(R.id.group_indicator);
        ViewStub stub = (ViewStub) convertView.findViewById(R.id.contentStub);
        stub.setLayoutResource(groupItemLayout);
        stub.inflate();
        groupViewHolder.contentView = convertView.findViewById(R.id.content);
        groupViewHolder.contentViewHolder = createGroupViewHolder(groupViewHolder.contentView);
        return groupViewHolder;
    }

    protected abstract MyBaseGroupViewHolder createGroupViewHolder(View convertView);

    protected abstract void fillGroupViewWithItem(MyBaseGroupViewHolder holder, T item, boolean isExpanded);

    public class MyBaseGroupViewHolder {

    }

    public class GroupViewHolder {
        ImageView groupIndicator;
        FrameLayout groupIndicatorFrame;
        MyBaseGroupViewHolder contentViewHolder;
        View contentView;
    }

    @Override
    public final View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expandable_child_content, null);
            holder = new ChildViewHolder();
            ViewStub stub = (ViewStub) convertView.findViewById(R.id.contentStub);
            stub.setLayoutResource(childItemLayout);
            stub.inflate();
            holder.contentView = convertView.findViewById(R.id.content);
            holder.contentViewHolder = createChildHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        holder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onContentClick(getChildId(groupPosition, childPosition));
            }
        });

        fillChildViewWithItem(holder.contentViewHolder, getChild(groupPosition, childPosition));
        return convertView;
    }

    protected abstract MyBaseChildViewHolder createChildHolder(View convertView);

    protected abstract void fillChildViewWithItem(MyBaseChildViewHolder holder, T item);

    public class MyBaseChildViewHolder {

    }

    public class ChildViewHolder {
        MyBaseChildViewHolder contentViewHolder;
        View contentView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Context getContext() {
        return context;
    }
}
