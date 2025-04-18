package com.sk.revisit;

import android.app.Application;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sk.revisit.activities.BaseActivity;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class Revisit extends Application {

    public static final AtomicLong requests = new AtomicLong(0);
    public static final AtomicLong resolved = new AtomicLong(0);
    public static final AtomicLong failed = new AtomicLong(0);
    public static final String TAG = Revisit.class.getSimpleName();
    public static final int MAX_THREADS = 8;
    private static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());
    public static boolean isNetworkAvailable = false;
    public static boolean shouldUpdate = false;
    MyUtils myUtils;
    MySettingsManager mySettingsManager;
    BaseActivity lastActivity;
    private Thread.UncaughtExceptionHandler mPreviousExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mySettingsManager = new MySettingsManager(this);
        myUtils = new MyUtils(this, mySettingsManager.getRootStoragePath());
        try {
            initCrashAnalytics();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize crash analytics", e);
            // Fallback to default error handling
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
                    Thread.currentThread(), e);
        }
    }

    private void initCrashAnalytics() {
        try {
            // Store previous handler for fallback
            mPreviousExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

            CrashAnalyticsThreadHandler crashHandler = new CrashAnalyticsThreadHandler(this);
            Thread.setDefaultUncaughtExceptionHandler(crashHandler);
            Log.d(TAG, "Crash analytics initialized successfully");

            // Post a check to main thread to verify handler is set
            MAIN_THREAD_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    Thread.UncaughtExceptionHandler currentHandler =
                            Thread.getDefaultUncaughtExceptionHandler();
                    if (currentHandler instanceof CrashAnalyticsThreadHandler) {
                        Log.d(TAG, "Crash handler verified on main thread");
                    } else {
                        Log.w(TAG, "Crash handler not set on main thread");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing crash analytics", e);
            // Restore previous handler if initialization fails
            if (mPreviousExceptionHandler != null) {
                Thread.setDefaultUncaughtExceptionHandler(mPreviousExceptionHandler);
            }
        }
    }

    public MySettingsManager getMySettingsManager() {
        return mySettingsManager;
    }

    public MyUtils getMyUtils() {
        return myUtils;
    }

    public BaseActivity getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(BaseActivity act) {
        lastActivity = act;
    }

    @Override
    public void onTerminate() {
        myUtils.shutdown();

        // Restore previous exception handler
        if (mPreviousExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(mPreviousExceptionHandler);
            Log.d(TAG, "Restored previous exception handler");
        }

        super.onTerminate();
    }

    /**
     * Custom uncaught exception handler that logs crash information and provides
     * analytics about crashes in the application.
     */
    private class CrashAnalyticsThreadHandler implements Thread.UncaughtExceptionHandler {
        private static final String TAG = "CrashAnalytics";
        private final Application mApplication;
        private final Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

        public CrashAnalyticsThreadHandler(Application application) {
            if (application == null) {
                throw new IllegalArgumentException("Application cannot be null");
            }
            mApplication = application;
            mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
            try {
                // Get stack trace as string
                Writer result = new StringWriter();
                PrintWriter printWriter = new PrintWriter(result);
                ex.printStackTrace(printWriter);
                String stackTrace = result.toString();
                printWriter.close();

                // Log crash details
                Log.e(TAG, "Uncaught exception in thread: " + thread.getName(), ex);
                Log.e(TAG, "Stack trace:\n" + stackTrace);

                // Additional crash analytics - you can add more analytics here
                String deviceInfo = "Device: " + android.os.Build.MODEL;
                String androidVersion = "Android Version: " + android.os.Build.VERSION.RELEASE;
                String appVersion = "App Version: N/A";
                try {
                    appVersion = "App Version: " + mApplication.getPackageManager()
                            .getPackageInfo(mApplication.getPackageName(), 0).versionName;
                } catch (Exception ignored) {
                }

                Log.e(TAG, deviceInfo);
                Log.e(TAG, androidVersion);
                Log.e(TAG, appVersion);

                // Save crash log to file
                saveCrashLogToFile(thread, stackTrace, deviceInfo, androidVersion, appVersion);

            } catch (Exception e) {
                Log.e(TAG, "Error while logging crash", e);
            }

            // Continue with default exception handling
            if (mDefaultExceptionHandler != null) {
                mDefaultExceptionHandler.uncaughtException(thread, ex);
            } else {
                System.exit(1);
            }
        }

        private void saveCrashLogToFile(Thread thread, String stackTrace, String deviceInfo, String androidVersion, String appVersion) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ENGLISH);
            String fileName = "crash_log_" + dateFormat.format(System.currentTimeMillis()) + ".txt";
            File crashFile = new File(mApplication.getObbDir(), fileName);
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Thread: ").append(thread.getName()).append("\n");
            logBuilder.append(deviceInfo).append("\n");
            logBuilder.append(androidVersion).append("\n");
            logBuilder.append(appVersion).append("\n");
            logBuilder.append("Stack trace:\n").append(stackTrace);
            try (FileOutputStream fos = new FileOutputStream(crashFile)) {
                fos.write(logBuilder.toString().getBytes());
                fos.flush();
            } catch (IOException e) {
                Log.e(TAG, "Failed to write crash log to file", e);
            }
        }
    }

}