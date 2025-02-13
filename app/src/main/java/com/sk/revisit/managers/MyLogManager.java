package com.sk.revisit.managers;

import android.content.Context;

import com.sk.revisit.log.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class MyLogManager {
	private static final String TAG = "MyLogManager";
	public Context context;
	private BufferedWriter writer;

	public MyLogManager(Context context, String filePath) {
		this.context = context;
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			this.writer = new BufferedWriter(new FileWriter(file, true));
		} catch (Exception e) {
			Log.e(TAG, "Error initializing MyLogManager: " + e.getMessage());
		}
	}

	public synchronized void log(String msg) {
		try {
			writer.write(msg);
			writer.newLine();
			writer.flush();
		} catch (Exception e) {
			Log.d(TAG, "Error writing log: " + e.getMessage());
		}
	}

	public synchronized void log(byte[] b) {
		try {
			writer.write(new String(b));
			writer.newLine();
			writer.flush();
		} catch (Exception e) {
			Log.e(TAG, "Error writing log bytes: " + e.getMessage());
		}
	}

	// Call this when shutting down the application
	public synchronized void close() {
		try {
			writer.close();
		} catch (Exception e) {
			Log.e(TAG, "Error closing log writer: " + e.getMessage());
		}
	}
}