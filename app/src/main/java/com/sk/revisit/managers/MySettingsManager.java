package com.sk.revisit.managers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class MySettingsManager {
	private static final String
			KEY_ROOT_PATH = "rootPath",
			KEY_THEME = "theme",
			KEY_DN_PATH = "dnPath",
			KEY_IS_FIRST = "isFirst",
			KEY_USR_AGENT_STR = "userAgentCustom",
			KEY_USR_AGENT_DEF = "userAgentDefaults",
			KEY_MAX_WEB_TIMEOUT = "webTimeoutDuration";

	private final SharedPreferences prefs;

	public MySettingsManager(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	void setTheme(String val) {
		putStr(KEY_THEME, val);
	}

	void setUserAgentCustom(String val) {
		putStr(KEY_USR_AGENT_STR, val);
	}

	public String getRootStoragePath() {
		return prefs.getString(KEY_ROOT_PATH, null);
	}

	public void setRootStoragePath(String folderPath) {
		putStr(KEY_ROOT_PATH, folderPath);
	}

	public String getDownloadStoragePath() {
		return prefs.getString(KEY_DN_PATH, null);
	}

	public boolean getIsFirst() {
		return prefs.getBoolean(KEY_IS_FIRST, true);
	}

	public void setIsFirst(boolean o) {
		putBoolean(KEY_IS_FIRST, o);
	}

	void putStr(String key, String val) {
		prefs.edit().putString(key, val).apply();
	}

	void putInt(String key, int val) {
		prefs.edit().putInt(key, val).apply();
	}

	void putBoolean(String key, boolean val) {
		prefs.edit().putBoolean(key, val).apply();
	}
}
