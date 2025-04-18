package com.sk.revisit;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.sk.revisit.data.UrlLog;
import com.sk.revisit.helper.FileHelper;
import com.sk.revisit.helper.LoggerHelper;
import com.sk.revisit.helper.MimeTypeHelper;
import com.sk.revisit.helper.NetHelper;
import com.sk.revisit.managers.SQLiteDBM;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyUtils {
    private static final String TAG = "MyUtils";
    private static final int BUFF_SIZE = 1024 * 8;
    public final OkHttpClient client;
    public final ExecutorService executorService;
    private final SQLiteDBM dbm;
    private final String rootPath;
    private final Context context;
    private final LoggerHelper logger;
    private final MimeTypeHelper mimeTypeHelper;
    private final NetHelper netHelper;
    private OnCreateLogListener onCreateLogListener;

    public MyUtils(Context context, String rootPath) {
        this.rootPath = rootPath;
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(Revisit.MAX_THREADS, new CustomThreadFactory());
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.dbm = new SQLiteDBM(context, rootPath + "/revisit.db");
        this.logger = new LoggerHelper(context, rootPath);
        this.mimeTypeHelper = new MimeTypeHelper(this);
        this.netHelper = new NetHelper(this.client);
    }

    public Context getContext() {
        return this.context;
    }

    public Response head(String url) {
        return this.netHelper.head(url);
    }

    public void log(String tag, String msg, Exception e) {
        logger.log(tag + "\t" + msg + "\t" + e.toString());
    }

    public void log(String tag, String msg) {
        logger.log(tag + "\t" + msg);
    }

    public void saveReq(String m) {
        logger.saveReq(m);
    }

    public void saveUrl(String uriStr) {
        logger.saveUrl(uriStr);
    }

    public void saveResp(String m) {
        logger.saveResp(m);
    }

    public void createUrlLog(UrlLog urlLog) {
        if (onCreateLogListener == null) return;
        onCreateLogListener.onCreate(urlLog);
    }

    public void setOnCreateLogListener(@NonNull OnCreateLogListener onCreateLogListener) {
        this.onCreateLogListener = onCreateLogListener;
    }

    /**
     * Builds a local file path based on the given URI.
     */
    public String buildLocalPath(@NonNull Uri uri) {
        String host = uri.getAuthority();
        String path = uri.getPath();
        String query = uri.getQuery();
        String lastPathSegment = uri.getLastPathSegment();
        StringBuilder localPathBuilder = new StringBuilder();

        if (query != null) {
            query = Base64.encodeToString(query.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        }

        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(path)) {
            log(TAG, "Invalid URI: Host or path is empty.");
            return null;
        }
        char sep = File.separatorChar;
        localPathBuilder.append(rootPath)
                .append(sep)
                .append(host)
                .append(sep)
                .append(path);

        if (query != null) {
            localPathBuilder.append('_').append(query);
        }

        if (lastPathSegment == null || !lastPathSegment.contains(".")) {
            localPathBuilder.append("/index.html");
        }

        String localPath = localPathBuilder.toString();

        return localPath.replaceAll("/+", "/");
    }

    @NonNull
    public String getMimeType(String filename) {
        String mimeType = "application/octet-stream";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filename);
        if (extension != null) {
            MimeTypeMap s = MimeTypeMap.getSingleton();
            if (s != null) {
                mimeType = s.getMimeTypeFromExtension(extension);
            }
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
        }
        return mimeType;
    }

    public void createMimeTypeMeta(Uri uri) {
        executorService.execute(() -> mimeTypeHelper.createMimeTypeMeta(uri));
    }

    public String getMimeTypeFromMeta(String filepath) {
        return mimeTypeHelper.getMimeTypeFromMeta(filepath);
    }

    public void createMimeTypeMetaFile(String filepath, String type) {
        executorService.execute(() -> mimeTypeHelper.createMimeTypeMetaFile(filepath, type));
    }

    /**
     * Downloads a resource from a URI to a local file using buffered streams.
     */
    public void download(@NonNull final Uri uri, @NonNull final DownloadListener listener) {
        executorService.execute(() -> {
            String localFilePath = buildLocalPath(uri);

            if (localFilePath == null) {
                listener.onFailure(new IOException("Failed to build local path for URI: " + uri));
                return;
            }

            File localFile = new File(localFilePath);

            if (localFile.exists() && !Revisit.shouldUpdate) {
                listener.onEnd(localFile);
                return;
            }

            Request request = new Request.Builder().url(uri.toString()).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    listener.onFailure(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        listener.onFailure(new IOException("Download failed. Response code: " + response.code()));
                        return;
                    }

                    File outfile = FileHelper.prepareFile(localFilePath);
                    long contentLength = response.body().contentLength();
                    if (contentLength == 0) {
                        contentLength = 1;
                    }

                    listener.onStart(uri, contentLength);

                    try (InputStream in = new BufferedInputStream(response.body().byteStream());
                         BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile))) {
                        byte[] buffer = new byte[BUFF_SIZE];
                        long bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, (int) bytesRead);
                            listener.onProgress((double) bytesRead / contentLength);
                        }
                        out.flush();

                        listener.onSuccess(outfile, response.headers());

                        // Store metadata asynchronously
                        executorService.execute(() -> {
                            MediaType mediaType = response.body().contentType();
                            String contentType = mediaType != null ? mediaType.toString() : "application/octet-stream";
                            createMimeTypeMetaFile(localFilePath, Objects.requireNonNull(contentType));
                            dbm.insertIntoUrlsIfNotExists(uri, localFilePath, new File(localFilePath).length(), response.headers());
                        });

                    } catch (Exception e) {
                        if (outfile.exists()) outfile.delete();
                        listener.onFailure(e);
                    }

                    listener.onEnd(outfile);
                }
            });
        });
    }

    /**
     * Shuts down the executors.
     */
    public void shutdown() {
        executorService.shutdown();
        logger.shutdown();
    }

    public String getRootPath() {
        return rootPath;
    }

    public interface OnCreateLogListener {
        void onCreate(UrlLog urlLog);
    }

    /**
     * Listener interface for download events.
     */
    public interface DownloadListener {
        void onStart(Uri uri, long contentLength);

        void onSuccess(File file, Headers headers);

        void onProgress(double p);

        void onFailure(Exception e);

        void onEnd(File file);
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private int count = 0;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MyUtils-Thread-" + count++);
        }
    }
}
