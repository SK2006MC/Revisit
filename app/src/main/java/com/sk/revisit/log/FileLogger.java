package com.sk.revisit.log;

import android.util.Log;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileLogger {
    private static final String TAG = "FileLogger";
    public String filePath;
    private BufferedWriter writer;

    public FileLogger(String filePath) {
        this.filePath = filePath;
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            this.writer = new BufferedWriter(new FileWriter(file, true));
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FileLogger: " + e.getMessage());
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