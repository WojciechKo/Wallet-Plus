package info.korzeniowski.walletplus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ProfileUtils {
    private static final String PREF_ACTIVE_PROFILE_ID = "pref_active_profile_id";

    public static boolean setActiveProfileId(final Context context, final Long id) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putLong(PREF_ACTIVE_PROFILE_ID, id).commit();
        return true;
    }

    public static Long getActiveProfileId(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getLong(PREF_ACTIVE_PROFILE_ID, -1);
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
