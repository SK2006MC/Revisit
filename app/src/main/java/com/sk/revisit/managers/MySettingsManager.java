package com.sk.revisit.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class MySettingsManager {
    private static final String PREF_NAME = "RevisitSettings";
    private static final String KEY_ROOT_PATH = "root_path";

    private final SharedPreferences prefs;

    public MySettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getRootStoragePath() {
        return prefs.getString(KEY_ROOT_PATH, null);
    }

    public void setRootStoragePath(String path) {
        prefs.edit().putString(KEY_ROOT_PATH, path).apply();
    }
}
