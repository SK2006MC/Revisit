package com.sk.revisit

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import android.webkit.MimeTypeMap
import com.sk.revisit.Consts
import com.sk.revisit.data.UrlLog
import com.sk.revisit.helper.FileHelper
import com.sk.revisit.helper.MimeHelper
import com.sk.revisit.helper.NetHelper
import com.sk.revisit.managers.SQLiteDBM
import okhttp3.*
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class MyUtils(val context: Context, val rootPath: String) {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    val executorService: ExecutorService = Executors.newFixedThreadPool(Revisit.MAX_THREADS, CustomThreadFactory())
    private val dbm: SQLiteDBM = SQLiteDBM(context, "$rootPath/${Consts.dbname}")
    private val MimeHelper: MimeHelper = MimeHelper(this)
    private val netHelper: NetHelper = NetHelper(this.client)
    var onCreateLogListener: OnCreateLogListener? = null

    fun head(url: String): Response? {
        return netHelper.head(url)
    }

    fun createUrlLog(urlLog: UrlLog) {
        onCreateLogListener?.onCreate(urlLog)
    }

    fun buildLocalPath(uri: Uri): String? {
        val host = uri.authority
        val path = uri.path
        var query = uri.query
        val lastPathSegment = uri.lastPathSegment
        val localPathBuilder = StringBuilder()

        if (query != null) {
            query = Base64.encodeToString(query.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        }

        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(path)) {
            return null
        }
        val sep = File.separatorChar
        localPathBuilder.append(rootPath)
            .append(sep)
            .append(host)
            .append(sep)
            .append(path)

        if (query != null) {
            localPathBuilder.append('_').append(query)
        }

        if (lastPathSegment == null || !lastPathSegment.contains(".")) {
            localPathBuilder.append("/index.html")
        }

        val localPath = localPathBuilder.toString()
        return localPath.replace("/+".toRegex(), "/")
    }

    fun getMimeType(filename: String): String {
        var mimeType = Consts.fallbackMime
        val extension = MimeTypeMap.getFileExtensionFromUrl(filename)
        if (extension != null) {
            val s = MimeTypeMap.getSingleton()
            if (s != null) {
                mimeType = s.getMimeTypeFromExtension(extension) ?: Consts.fallbackMime
            }
        }
        return mimeType
    }

    fun download(uri: Uri, listener: DownloadListener) {
        executorService.execute {
            val localFilePath = buildLocalPath(uri)

            if (localFilePath == null) {
                listener.onFailure(IOException("Failed to build local path for URI: $uri"))
                return@execute
            }

            val localFile = File(localFilePath)

            if (localFile.exists() && !Revisit.shouldUpdate) {
                listener.onEnd(localFile)
                return@execute
            }

            val request = Request.Builder().url(uri.toString()).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    listener.onFailure(e)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        listener.onFailure(IOException("Download failed. Response code: ${response.code}"))
                        return
                    }

                    val outfile = FileHelper.prepareFile(localFilePath)
                    var contentLength = body.contentLength()
                    if (contentLength == 0L) {
                        contentLength = 1
                    }

                    listener.onStart(uri, contentLength)

                    try {
                        BufferedInputStream(body.byteStream()).use { `in` ->
                            BufferedOutputStream(FileOutputStream(outfile)).use { out ->
                                val buffer = ByteArray(BUFF_SIZE)
                                var bytesRead: Int
                                while (`in`.read(buffer).also { bytesRead = it } != -1) {
                                    out.write(buffer, 0, bytesRead)
                                    listener.onProgress(bytesRead.toDouble() / contentLength)
                                }
                                out.flush()
                            }
                        }

                        listener.onSuccess(outfile, response.headers)

                        executorService.execute {
                            val mediaType = body.contentType()
                            val contentType = mediaType?.toString() ?: "application/octet-stream"
                            // createMimeTypeMetaFile(localFilePath, contentType)
                            dbm.insertIntoUrlsIfNotExists(uri, localFilePath, File(localFilePath).length(), response.headers)
                        }

                    } catch (e: Exception) {
                        if (outfile.exists()) outfile.delete()
                        listener.onFailure(e)
                    }

                    listener.onEnd(outfile)
                }
            })
        }
    }

    fun shutdown() {
        executorService.shutdown()
    }

    fun interface OnCreateLogListener {
        fun onCreate(urlLog: UrlLog)
    }

    interface DownloadListener {
        fun onStart(uri: Uri, contentLength: Long)
        fun onSuccess(file: File, headers: Headers)
        fun onProgress(p: Double)
        fun onFailure(e: Exception)
        fun onEnd(file: File)
    }

    private class CustomThreadFactory : ThreadFactory {
        private var count = 0
        override fun newThread(r: Runnable): Thread {
            return Thread(r, "MyUtils-Thread-${count++}")
        }
    }

    companion object {
        protected val TAG: String = this::class.java.simpleName
        private const val BUFF_SIZE = 1024 * 8
    }
}
