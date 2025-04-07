package com.sk.revisit.managers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class MySettingsManager {
	private static final String KEY_ROOT_PATH = "root_path";
	private static final String KEY_DN_PATH = "dn_path";
	private static final String KEY_IS_FIRST = "is_first";
	private final SharedPreferences prefs;

	public MySettingsManager(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public String getRootStoragePath() {
		return prefs.getString(KEY_ROOT_PATH, null);
	}

	public void setRootStoragePath(String folderPath) {
		prefs.edit().putString(KEY_ROOT_PATH, folderPath).apply();
	}

	public String getDownloadStoragePath() {
		return prefs.getString(KEY_DN_PATH, null);
	}

	public boolean getIsFirst() {
		return prefs.getBoolean(KEY_IS_FIRST, true);
	}

	public void setIsFirst(boolean o) {
		prefs.edit().putBoolean(KEY_IS_FIRST, o).apply();
	}

}
