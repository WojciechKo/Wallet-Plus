package com.walletudo.util;

import android.content.Context;

public class Utils {
    private static final String MAIN_DATABASES_FOLDER = "databases/";
    public static final String PROFILES_DATABASE_FOLDER = "profiles/";
    public static final String DATABASE_EXTENSION = ".db";

    public static String getProfileDatabaseFilePath(Context context, String name) {
        return getMainDatabaseFolder(context) + getProfileDatabaseName(name);
    }

    public static String getProfileDatabaseName(String name) {
        return PROFILES_DATABASE_FOLDER + name + DATABASE_EXTENSION;
    }

    public static String getMainDatabaseFolder(Context context) {
        return getDataDir(context) + "/" + MAIN_DATABASES_FOLDER;
    }

    public static String getDataDir(Context context) {
        return context.getApplicationInfo().dataDir;
    }
}
