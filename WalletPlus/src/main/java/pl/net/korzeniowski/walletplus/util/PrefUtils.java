package pl.net.korzeniowski.walletplus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

    /**
     * Last picked color hue for tag.
     */
    private static final String PREF_LAST_TAG_COLOR_HUE = "pref_last_tag_color_hue";

    private final Context context;

    public PrefUtils(Context context) {
        this.context = context;
    }

    public boolean isWelcomeDone() {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public void markWelcomeDone() {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }

    public boolean isDataBootstrapDone() {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }

    public void markDataBootstrapDone() {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).commit();
    }

    public Long getActiveProfileId() {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getLong(PREF_ACTIVE_PROFILE_ID, -1);
    }

    public boolean setActiveProfileId(final Long id) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putLong(PREF_ACTIVE_PROFILE_ID, id).commit();
        return true;
    }

    public String getGoogleToken() {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString(GOOGLE_TOKEN, "");
    }

    public void setGoogleToken(final String token) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(GOOGLE_TOKEN, token).commit();
    }

    public int getNextTagColor() {
        SharedPreferences sp = getSharedPreferences(context);
        float goldenRatio = (float) 0.618033988749895;
        float nextHue = (sp.getFloat(PREF_LAST_TAG_COLOR_HUE, 0) + goldenRatio) % 1;
        sp.edit().putFloat(PREF_LAST_TAG_COLOR_HUE, nextHue).commit();

        float[] hsv = new float[3];
        hsv[0] = nextHue * 360; // Hue (0 .. 360)
        hsv[1] = (float) 0.55; // Saturation (0 .. 1)
        hsv[2] = (float) 0.90; // Value (0 .. 1)
        return Color.HSVToColor(hsv);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
