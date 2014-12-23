package info.korzeniowski.walletplus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AccountUtils {
    private static final String PREF_ACTIVE_ACCOUNT = "chosen_account";

    public static boolean setActiveAccountId(final Context context, final Long id) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putLong(PREF_ACTIVE_ACCOUNT, id).commit();
        return true;
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Long getActiveAccountId(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getLong(PREF_ACTIVE_ACCOUNT, -1);
    }
}
