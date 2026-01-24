package com.sk.revisit.managers

import android.net.Uri
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.sk.revisit.MyUtils
import com.sk.revisit.Revisit
import com.sk.revisit.log.FileLogger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import okhttp3.Headers
import java.io.*

class WebStorageManager(private val utils: MyUtils) {

    val urlLogger: FileLogger
    var requestLogger: FileLogger
    val loggingExecutor: ExecutorService = Executors.newSingleThreadExecutor(LoggingThreadFactory())

    fun saveUrl(url: String){
        executorService.execute(()->{
            urlLogger.log(url)
        })
    }

    fun getResponse(request: WebResourceRequest): WebResourceResponse? {
        Revisit.requests.incrementAndGet()

        if (GET_METHOD != request.method) {
            return null
        }

        val uri = request.url
        val uriStr = uri.toString()
        saveUrl(uriStr)

        if (!URLUtil.isNetworkUrl(uriStr)) {
            return null
        }

        val localPath = utils.buildLocalPath(uri) ?: return null

        val localFile = File(localPath)
        val fileExists = localFile.exists()

        return if (fileExists) {
            if (Revisit.shouldUpdate && Revisit.isNetworkAvailable) {
                utils.download(uri, createDownloadListener(uriStr, localPath))
            }
            loadFromLocal(localFile, uri)
        } else {
            if (Revisit.isNetworkAvailable) {
                utils.download(uri, createDownloadListener(uriStr, localPath))
                loadFromLocal(localFile, uri)
            } else {
                saveReq(uriStr)
                createNoOfflineFileResponse()
            }
        }
    }

    private fun createDownloadListener(uriStr: String, localPath: String): MyUtils.DownloadListener {
        return object : MyUtils.DownloadListener {
            override fun onStart(uri: Uri, contentLength: Long) {
                // ToDo create a item_url_log
            }

            override fun onSuccess(file: File, headers: Headers) {
                Revisit.resolved.incrementAndGet()
            }

            override fun onProgress(p: Double) {
                // urlLog.pb.setProgress(p)
            }

            override fun onFailure(e: Exception) {
                Revisit.failed.incrementAndGet()
                saveReq(uriStr)
            }

            override fun onEnd(file: File) {
                // urllog.pb.visible=gone
            }
        }
    }

    private fun loadFromLocal(localFile: File, uri: Uri): WebResourceResponse? {
        val localFilePath:String = localFile.absolutePath
        return try {
            val mimeType:String = getMimeType(localFilePath, uri)
            Revisit.resolved.incrementAndGet()
            val inputStream = FileInputStream(localFile)
            val response = WebResourceResponse(mimeType, UTF_8, inputStream)
            response.responseHeaders = mapOf("Access-Control-Allow-Origin" to "*")
            response
        } catch (e: FileNotFoundException) {
            Revisit.failed.incrementAndGet()
            null
        }
    }

    private fun getMimeType(localFilePath: String, uri: Uri): String {
        var mimeType:String = utils.getMimeTypeFromMeta(localFilePath)
        if (mimeType == null) {
            utils.createMimeTypeMeta(uri)
            mimeType = utils.getMimeType(localFilePath)
        }
        return mimeType
    }

    private fun createNoOfflineFileResponse(): WebResourceResponse {
        return WebResourceResponse(
            DEFAULT_MIME,
            UTF_8,
            ByteArrayInputStream(NO_OFFLINE_FILE_MESSAGE.toByteArray())
        )
    }

    companion object {
        private const val DEFAULT_MIME = "text/html"
        private const val GET_METHOD = "GET"
        private const val UTF_8 = "UTF-8"
        private const val NO_OFFLINE_FILE_MESSAGE = "No offline file available."
        protected val TAG: String = this::class.java.simpleName
    }

    // Thread factory for logging tasks (lower priority)
    private class LoggingThreadFactory : ThreadFactory {
        fun newThread(r: Runnable): Thread {
            val t : Thread = Thread(r, "MyUtils-Logging-Thread")
            t.priority = Thread.MIN_PRIORITY
            return t
        }
    }
}
