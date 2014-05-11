package info.korzeniowski.walletplus.drawermenu.record;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import info.korzeniowski.walletplus.model.Record;

public class RecordListAdapter extends ArrayAdapter<Record> {

    public RecordListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public RecordListAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public RecordListAdapter(Context context, int resource, Record[] objects) {
        super(context, resource, objects);
    }

    public RecordListAdapter(Context context, int resource, int textViewResourceId, Record[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public RecordListAdapter(Context context, int resource, List<Record> objects) {
        super(context, resource, objects);
    }

    public RecordListAdapter(Context context, int resource, int textViewResourceId, List<Record> objects) {
        super(context, resource, textViewResourceId, objects);
    }
}
