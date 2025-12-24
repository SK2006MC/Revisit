package com.sk.revisit

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.sk.revisit.activities.BaseActivity
import com.sk.revisit.managers.MySettingsManager
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class Revisit : Application() {

    var myUtils: MyUtils? = null
    var lastActivity: BaseActivity? = null
    lateinit var mySettingsManager: MySettingsManager
    private var mPreviousExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun onCreate() {
        super.onCreate()
        mySettingsManager = MySettingsManager(this)
        myUtils = MyUtils(this, mySettingsManager.rootStoragePath)
        try {
            initCrashAnalytics()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize crash analytics", e)
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(
                Thread.currentThread(), e
            )
        }
    }

    private fun initCrashAnalytics() {
        try {
            mPreviousExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            val crashHandler = CrashAnalyticsThreadHandler(this)
            Thread.setDefaultUncaughtExceptionHandler(crashHandler)
            Log.d(TAG, "Crash analytics initialized successfully")

            MAIN_THREAD_HANDLER.post {
                val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
                if (currentHandler is CrashAnalyticsThreadHandler) {
                    Log.d(TAG, "Crash handler verified on main thread")
                } else {
                    Log.w(TAG, "Crash handler not set on main thread")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing crash analytics", e)
            if (mPreviousExceptionHandler != null) {
                Thread.setDefaultUncaughtExceptionHandler(mPreviousExceptionHandler)
            }
        }
    }

    override fun onTerminate() {
        myUtils?.shutdown()
        if (mPreviousExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(mPreviousExceptionHandler)
            Log.d(TAG, "Restored previous exception handler")
        }
        super.onTerminate()
    }

    private inner class CrashAnalyticsThreadHandler(private val mApplication: Application) :
        Thread.UncaughtExceptionHandler {

        private val mDefaultExceptionHandler: Thread.UncaughtExceptionHandler? =
            Thread.getDefaultUncaughtExceptionHandler()

        override fun uncaughtException(thread: Thread, ex: Throwable) {
            try {
                val result: Writer = StringWriter()
                val printWriter = PrintWriter(result)
                ex.printStackTrace(printWriter)
                val stackTrace = result.toString()
                printWriter.close()

                Log.e(TAG_CRASH, "Uncaught exception in thread: ${thread.name}", ex)
                Log.e(TAG_CRASH, "Stack trace:\n$stackTrace")

                val deviceInfo = "Device: ${android.os.Build.MODEL}"
                val androidVersion = "Android Version: ${android.os.Build.VERSION.RELEASE}"
                var appVersion = "App Version: N/A"
                try {
                    appVersion = "App Version: ${
                        mApplication.packageManager.getPackageInfo(
                            mApplication.packageName,
                            0
                        ).versionName
                    }"
                } catch (ignored: Exception) {
                }

                Log.e(TAG_CRASH, deviceInfo)
                Log.e(TAG_CRASH, androidVersion)
                Log.e(TAG_CRASH, appVersion)

                saveCrashLogToFile(thread, stackTrace, deviceInfo, androidVersion, appVersion)

            } catch (e: Exception) {
                Log.e(TAG_CRASH, "Error while logging crash", e)
            }

            if (mDefaultExceptionHandler != null) {
                mDefaultExceptionHandler.uncaughtException(thread, ex)
            } else {
                System.exit(1)
            }
        }

        private fun saveCrashLogToFile(
            thread: Thread,
            stackTrace: String,
            deviceInfo: String,
            androidVersion: String,
            appVersion: String
        ) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ENGLISH)
            val fileName = "crash_log_${dateFormat.format(System.currentTimeMillis())}.txt"
            val crashFile = File(mApplication.obbDir, fileName)
            val logBuilder = StringBuilder()
            logBuilder.append("Thread: ").append(thread.name).append("\n")
            logBuilder.append(deviceInfo).append("\n")
            logBuilder.append(androidVersion).append("\n")
            logBuilder.append(appVersion).append("\n")
            logBuilder.append("Stack trace:\n").append(stackTrace)
            try {
                FileOutputStream(crashFile).use { fos ->
                    fos.write(logBuilder.toString().toByteArray())
                    fos.flush()
                }
            } catch (e: IOException) {
                Log.e(TAG_CRASH, "Failed to write crash log to file", e)
            }
        }
    }

    companion object {
        val requests = AtomicLong(0)
        val resolved = AtomicLong(0)
        val failed = AtomicLong(0)
        val TAG: String = Revisit::class.java.simpleName
        private const val TAG_CRASH = "CrashAnalytics"
        const val MAX_THREADS = 8
        private val MAIN_THREAD_HANDLER = Handler(Looper.getMainLooper())
        var isNetworkAvailable = false
        var shouldUpdate = false
    }
}
