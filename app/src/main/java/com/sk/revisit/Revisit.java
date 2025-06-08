package com.sk.revisit;

import android.app.Application;

import com.sk.revisit.managers.MySettingsManager;

public class Revisit extends Application {

	MyUtils myUtils;
	MySettingsManager mySettingsManager;

	@Override
	public void onCreate() {
		super.onCreate();
		mySettingsManager = new MySettingsManager(this);
		myUtils = new MyUtils(this, mySettingsManager.getRootStoragePath());
	}

	public MySettingsManager getMySettingsManager() {
		return mySettingsManager;
	}

	public MyUtils getMyUtils() {
		return myUtils;
	}

	@Override
	public void onTerminate() {
		myUtils.shutdown();
		super.onTerminate();
	}
}