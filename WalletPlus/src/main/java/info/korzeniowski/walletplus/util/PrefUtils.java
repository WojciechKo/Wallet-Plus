package info.korzeniowski.walletplus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
    /**
     * Boolean indicating whether we performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    /**
     * Boolean indicating whether we installed the boostrap data or not.
     */
    public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

    /**
     * String containing Google auth token.
     */
    private static final String GOOGLE_TOKEN = "pref_google_token";

    /**
     * Long containing id of Profile that is active.
     */
    private static final String PREF_ACTIVE_PROFILE_ID = "pref_active_profile_id";

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }

    public static boolean isDataBootstrapDone(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }

    public static void markDataBootstrapDone(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).commit();
    }

    public static Long getActiveProfileId(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getLong(PREF_ACTIVE_PROFILE_ID, -1);
    }

    public static boolean setActiveProfileId(final Context context, final Long id) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putLong(PREF_ACTIVE_PROFILE_ID, id).commit();
        return true;
    }

    public static String getGoogleToken(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString(GOOGLE_TOKEN, "");
    }

    public static void setGoogleToken(final Context context, final String token) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(GOOGLE_TOKEN, token).commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
