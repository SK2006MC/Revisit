package com.sk.revisit.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class MySettingsManager {
	private static final String PREF_NAME = "RevisitSettings";
	private static final String KEY_ROOT_PATH = "root_path";
	private static final String KEY_DN_PATH = "dn_path";
	private static final String KEY_IS_FIRST = "is_first";
	private static String reqFileName = "req.txt";

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

	public String getDownloadStoragePath() {
		return prefs.getString(KEY_DN_PATH, null);
	}

	public void setDownloadStoragePath(String path) {
		prefs.edit().putString(KEY_ROOT_PATH, path).apply();
	}

	public boolean getIsFirst() {
		return prefs.getBoolean(KEY_IS_FIRST, true);
	}

	public void setIsFirst(boolean o) {
		prefs.edit().putBoolean(KEY_IS_FIRST, o).apply();
	}

	public String getReqFileName() {
		return reqFileName;
	}

	public void setReqFileName(String reqFileName) {
		MySettingsManager.reqFileName = reqFileName;
	}
}
