package com.sk.revisit.helper

import android.content.Context
import android.util.Base64
import com.sk.revisit.log.FileLogger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class LogHelper(context: Context, rootPath: String) {
    private val myfileLogger: FileLogger = FileLogger("$rootPath/log.txt")
    private val req: FileLogger = FileLogger("$rootPath/req.txt")
    private val resp: FileLogger = FileLogger("$rootPath/saved.base64")
    private val urls: FileLogger = FileLogger("$rootPath/urls.txt")
    private val loggingExecutor: ExecutorService = Executors.newSingleThreadExecutor(LoggingThreadFactory())

    fun log(msg: String) {
        loggingExecutor.execute { myfileLogger.log(msg) }
    }

    fun saveReq(msg: String) {
        loggingExecutor.execute { req.log(msg) }
    }

    fun saveResp(msg: String) {
        loggingExecutor.execute {
            resp.log(Base64.encodeToString(msg.toByteArray(), Base64.NO_WRAP) + "\n----\n")
        }
    }

    fun saveUrl(uriStr: String) {
        loggingExecutor.execute { urls.log(uriStr) }
    }

    fun shutdown() {
        loggingExecutor.shutdown()
        urls.close()
        req.close()
        resp.close()
        myfileLogger.close()
    }

    // Thread factory for logging tasks (lower priority)
    private class LoggingThreadFactory : ThreadFactory {
        override fun newThread(r: Runnable): Thread {
            val t = Thread(r, "MyUtils-Logging-Thread")
            t.priority = Thread.MIN_PRIORITY
            return t
        }
    }
}
