package com.sk.revisit.managers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

enum KEY {
    ROOT_PATH,
    THEME,
    DN_PATH,
    IS_FIRST,
    USR_AGENT_STR,
    USR_AGENT_DEF,
    MAX_WEB_TIMEOUT,
}

public class MySettingsManager extends SettingsHelp {

    public MySettingsManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //getters
    public String getRootStoragePath() {
        return getStr(KEY.ROOT_PATH, "/sdcard/Android/obb/com.sk.revisit");
    }

    //setters
    public void setRootStoragePath(String folderPath) {
        putStr(KEY.ROOT_PATH, folderPath);
    }

    public String getDownloadStoragePath() {
        return getStr(KEY.DN_PATH, null);
    }

    public boolean getIsFirst() {
        return getBoolean(KEY.IS_FIRST, true);
    }

    public void setIsFirst(boolean val) {
        putBoolean(KEY.IS_FIRST, val);
    }

    void setTheme(String val) {
        putStr(KEY.THEME, val);
    }

    void setUserAgentCustom(String val) {
        putStr(KEY.USR_AGENT_STR, val);
    }

}

class SettingsHelp {
    SharedPreferences prefs;

    //getters
    String getStr(@NonNull KEY key, String def) {
        return prefs.getString(toStr(key), def);
    }

    boolean getBoolean(@NonNull KEY key, boolean def) {
        return prefs.getBoolean(toStr(key), def);
    }

    //setters
    void putStr(@NonNull KEY key, String val) {
        prefs.edit().putString(toStr(key), val).apply();
    }

    void putInt(@NonNull KEY key, int val) {
        prefs.edit().putInt(toStr(key), val).apply();
    }

    void putBoolean(@NonNull KEY key, boolean val) {
        prefs.edit().putBoolean(toStr(key), val).apply();
    }

    //helpers
    String toStr(@NonNull KEY val) {
        return val.name();
    }
}
