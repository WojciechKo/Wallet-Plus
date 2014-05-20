package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import info.korzeniowski.walletplus.model.CashFlow;

public class CashFlowListAdapter extends ArrayAdapter<CashFlow> {

    public CashFlowListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public CashFlowListAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public CashFlowListAdapter(Context context, int resource, CashFlow[] objects) {
        super(context, resource, objects);
    }

    public CashFlowListAdapter(Context context, int resource, int textViewResourceId, CashFlow[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public CashFlowListAdapter(Context context, int resource, List<CashFlow> objects) {
        super(context, resource, objects);
    }

    public CashFlowListAdapter(Context context, int resource, int textViewResourceId, List<CashFlow> objects) {
        super(context, resource, textViewResourceId, objects);
    }
}
