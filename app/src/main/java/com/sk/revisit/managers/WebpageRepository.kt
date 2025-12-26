package com.sk.revisit.managers

import android.content.Context
import android.text.format.Formatter
import android.util.Log
import com.sk.revisit.data.ItemPage
import com.sk.revisit.helper.FileHelper
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class WebpageRepository(private val context: Context, private val rootPath: String?) {

    private val folderSizeCache = ConcurrentHashMap<String, Long>()
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    interface Callback {
        fun onSuccess(pages: List<ItemPage>)
        fun onError(message: String)
    }

    fun loadWebpages(callback: Callback) {
        if (rootPath.isNullOrEmpty()) {
            callback.onError("Error: Invalid storage path.")
            return
        }

        val rootDir = File(rootPath)
        if (!rootDir.exists() || !rootDir.isDirectory) {
            callback.onError("Error: Invalid storage directory.")
            return
        }

        // Use Coroutines instead of ExecutorService
        repositoryScope.launch {
            try {
                val webPages = withContext(Dispatchers.IO) {
                    val htmlFilesPaths = mutableListOf<String>()

                    // Assuming FileHelper is now a Kotlin 'object'
                    FileHelper.searchRecursive(rootDir, HTML_EXTENSION, htmlFilesPaths)

                    htmlFilesPaths.map { fullPath ->
                        val relativePath = fullPath.replace("$rootPath${File.separator}", "")
                        val segments = relativePath.split(File.separator)

                        ItemPage().apply {
                            host = if (segments.isNotEmpty()) segments[0] else "unknown_host"
                            fileName = relativePath
                            size = calcSize(fullPath)
                            sizeStr = Formatter.formatFileSize(context, size)
                        }
                    }
                }
                callback.onSuccess(webPages)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading webpages", e)
                callback.onError("Failed to load pages: ${e.message}")
            }
        }
    }

    private fun calcSize(fullPath: String): Long {
        val file = File(fullPath)
        val parentDir = file.parent ?: return -1

        // Check cache first
        folderSizeCache[parentDir]?.let { return it }

        return try {
            val size = FileHelper.getFolderSize(parentDir)
            folderSizeCache[parentDir] = size
            size
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating size for $parentDir", e)
            -1
        }
    }

    fun shutdown() {
        repositoryScope.cancel() // Cancels all pending background tasks
    }

    companion object {
        private const val HTML_EXTENSION = ".html"
        private const val TAG = "WebpageRepository"
    }
}