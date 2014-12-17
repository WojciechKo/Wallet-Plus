package info.korzeniowski.walletplus.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.Childable;
import info.korzeniowski.walletplus.model.Identifiable;

public abstract class IdentifiableExpandableListAdapter<T extends Identifiable & Childable<T>> extends BaseExpandableListAdapter {
    private final Context context;
    private final List<T> items;
    private final int groupItemLayout;
    private final int childItemLayout;
    private final OnContentClickListener<T> clickListener;
    private OnContentLongClickListener<T> longClickListener;

    protected IdentifiableExpandableListAdapter(Context context, List<T> items, int groupItemLayout, int childItemLayout, OnContentClickListener<T> clickListener) {
        this.context = context;
        this.items = items;
        this.groupItemLayout = groupItemLayout;
        this.childItemLayout = childItemLayout;
        this.clickListener = clickListener;
    }

    protected IdentifiableExpandableListAdapter(Context context, List<T> items, int groupItemLayout, int childItemLayout, OnContentClickListener<T> clickListener, OnContentLongClickListener<T> longClickListener) {
        this(context, items, groupItemLayout, childItemLayout, clickListener);
        this.longClickListener = longClickListener;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.expandable_group_content, parent, false);
            groupViewHolder = getGroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }

        if (groupPosition == 0) {
            groupViewHolder.divider.setVisibility(View.INVISIBLE);
        } else {
            groupViewHolder.divider.setVisibility(View.VISIBLE);
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

    private GroupViewHolder getGroupViewHolder(View convertView) {
        GroupViewHolder groupViewHolder = new GroupViewHolder();
        groupViewHolder.divider = (ImageView) convertView.findViewById(R.id.divider);
        groupViewHolder.content = convertView.findViewById(R.id.group_content);
        groupViewHolder.groupIndicator = (ImageView) convertView.findViewById(R.id.group_indicator);
        ViewStub stub = (ViewStub) convertView.findViewById(R.id.content_stub);
        stub.setLayoutResource(groupItemLayout);
        groupViewHolder.contentViewHolder = createGroupViewHolder(stub.inflate());
        return groupViewHolder;
    }

    private void setupContent(GroupViewHolder groupViewHolder, final int groupPosition) {
        groupViewHolder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onContentClick(getGroup(groupPosition));
            }
        });
        groupViewHolder.content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onContentLongClick(getGroup(groupPosition));
                return true;
            }
        });
    }

    private void setupIndicatorAsExpandable(GroupViewHolder groupViewHolder, final ExpandableListView listView, final int groupPosition) {
        groupViewHolder.groupIndicator.setImageResource(listView.isGroupExpanded(groupPosition) ? R.drawable.arrow_down : R.drawable.arrow_left);
        groupViewHolder.groupIndicator.setOnClickListener(new View.OnClickListener() {
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
        groupViewHolder.groupIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onContentClick(getGroup(groupPosition));
            }
        });
        groupViewHolder.groupIndicator.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onContentLongClick(getGroup(groupPosition));
                return true;
            }
        });
    }

    protected abstract MyBaseGroupViewHolder createGroupViewHolder(View convertView);

    protected abstract void fillGroupViewWithItem(MyBaseGroupViewHolder holder, T item, boolean isExpanded);

    public class MyBaseGroupViewHolder {

    }

    public class GroupViewHolder {
        ImageView divider;
        ImageView groupIndicator;
        View content;
        MyBaseGroupViewHolder contentViewHolder;
    }

    @Override
    public final View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expandable_child_content, parent, false);
            holder = getChildViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onContentClick(getChild(groupPosition, childPosition));
            }
        });

        holder.content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onContentLongClick(getChild(groupPosition, childPosition));
                return true;
            }
        });

        fillChildViewWithItem(holder.contentViewHolder, getChild(groupPosition, childPosition));
        return convertView;
    }

    private ChildViewHolder getChildViewHolder(View convertView) {
        ChildViewHolder holder = new ChildViewHolder();
        holder.divider = (ImageView) convertView.findViewById(R.id.divider);
        holder.content = convertView.findViewById(R.id.child_content);
        ViewStub stub = (ViewStub) convertView.findViewById(R.id.content_stub);
        stub.setLayoutResource(childItemLayout);
        holder.contentViewHolder = createChildHolder(stub.inflate());
        return holder;
    }

    protected abstract MyBaseChildViewHolder createChildHolder(View convertView);

    protected abstract void fillChildViewWithItem(MyBaseChildViewHolder holder, T item);

    public class MyBaseChildViewHolder {

    }

    public class ChildViewHolder {
        ImageView divider;
        View content;
        MyBaseChildViewHolder contentViewHolder;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    protected Context getContext() {
        return context;
    }
}
