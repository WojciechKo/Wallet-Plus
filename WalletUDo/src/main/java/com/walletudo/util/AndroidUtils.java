package com.walletudo.util;

import android.app.Activity;
import android.content.Intent;

import com.walletudo.Walletudo;
import com.walletudo.ui.dashboard.DashboardActivity;

public class AndroidUtils {
    public static void restartApplication(Activity activity) {
        ((Walletudo) activity.getApplication()).reinitializeObjectGraph();
        activity.startActivity(new Intent(activity, DashboardActivity.class));
        activity.finish();
    }
}
