package com.sk.revisit.log

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class FileLogger(val filePath: String) {
    private var writer: BufferedWriter? = null

    init {
        val file = File(filePath)
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            writer = BufferedWriter(FileWriter(file, true))
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FileLogger: ${e.message}")
        }
    }

    @Synchronized
    fun log(msg: String) {
        try {
            writer?.let {
                it.write(msg)
                it.newLine()
                it.flush()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error writing log: ${e.message}")
        }
    }

    @Synchronized
    fun log(b: ByteArray) {
        try {
            writer?.let {
                it.write(String(b))
                it.newLine()
                it.flush()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing log bytes: ${e.message}")
        }
    }

    // Call this when shutting down the application
    @Synchronized
    fun close() {
        try {
            writer?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing log writer: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "FileLogger"
    }
}
