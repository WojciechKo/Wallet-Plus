package com.walletudo.util;


import android.content.Context;
import android.content.SharedPreferences;

public class StateManager {
    private static final String CASH_FLOW_LIST_PREFERENCES = "pref-cash-flow-list";

    private static final String STATE_LIST_ITEM_KEY = "state-list-item";
    private static final String STATE_LIST_ITEM_SCROLL_KEY = "state-list-item-scroll";

    public static void setCashFlowListState(Context context, CashFlowListState state) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(STATE_LIST_ITEM_KEY, state.getListItem());
        editor.putInt(STATE_LIST_ITEM_SCROLL_KEY, state.getListItemScroll());
        editor.commit();
    }

    public static CashFlowListState getCashFlowListState(Context context) {
        SharedPreferences preferences = getPreferences(context);
        int listItem = preferences.getInt(STATE_LIST_ITEM_KEY, 0);
        int listItemScroll = preferences.getInt(STATE_LIST_ITEM_SCROLL_KEY, 0);
        return new CashFlowListState(listItem, listItemScroll);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(CASH_FLOW_LIST_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static void clearStates(Context context) {
        setCashFlowListState(context, new CashFlowListState(0, 0));
    }

    public static class CashFlowListState {
        int listItem;
        int listItemScroll;

        public CashFlowListState(int listItem, int listItemScroll) {
            this.listItem = listItem;
            this.listItemScroll = listItemScroll;
        }

        public int getListItem() {
            return listItem;
        }

        public int getListItemScroll() {
            return listItemScroll;
        }
    }
}
