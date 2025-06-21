package com.sk.revisit.helper;

import android.util.Base64;

import com.sk.revisit.log.FileLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class LoggerHelper {
    private final FileLogger myfileLogger;
    private final FileLogger req;
    private final FileLogger resp;
    private final FileLogger urls;
    private final ExecutorService loggingExecutor;

    public LoggerHelper(android.content.Context context, String rootPath) {
        this.myfileLogger = new FileLogger(rootPath + "/log.txt");
        this.req = new FileLogger(rootPath + "/req.txt");
        this.urls = new FileLogger(rootPath + "/urls.txt");
        this.resp = new FileLogger(rootPath + "/saved.base64");

        this.loggingExecutor = Executors.newSingleThreadExecutor(new LoggingThreadFactory());
    }

    public void log(String msg) {
        loggingExecutor.execute(() -> myfileLogger.log(msg));
    }

    public void saveReq(String msg) {
        loggingExecutor.execute(() -> req.log(msg));
    }

    public void saveResp(String msg) {
        loggingExecutor.execute(() -> resp.log(Base64.encodeToString(msg.getBytes(), Base64.NO_WRAP) + "\n----\n"));
    }

    public void saveUrl(String uriStr) {
        loggingExecutor.execute(() -> urls.log(uriStr));
    }

    public void shutdown() {
        loggingExecutor.shutdown();
        urls.close();
        req.close();
        resp.close();
        myfileLogger.close();

    }

    // Thread factory for logging tasks (lower priority)
    private static class LoggingThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "MyUtils-Logging-Thread");
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }
}