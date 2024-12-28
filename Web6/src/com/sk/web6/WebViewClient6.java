package com.sk.web6;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ToggleButton;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebViewClient6 extends WebViewClient {

    private final Context context;
    private final String rootPath;
    private final ArrayList<String> log;
    private final Map<String, String> mimeTypeCache = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    ToggleButton b;
    public WebViewClient6(Context context, String rootPath, ArrayList<String> log,ToggleButton b) {
        this.context = context;
        this.rootPath = rootPath;
        this.log = log;
        this.b =b ;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
        
        if (!URLUtil.isNetworkUrl(uri.toString())) {
            log.add("not network url");
            return null;
        }
        
        String localPathT = rootPath + uri.getHost() + uri.getEncodedPath();
        String localPath;
        
        if(uri.getLastPathSegment()!=null&&!uri.getLastPathSegment().contains(".")){
            if(uri.toString().endsWith("/")){
                localPath=localPathT+"index.html";
            }else{
                localPath=localPathT+"/index.html";
            }
        }else if(uri.getLastPathSegment()==null){
            localPath=localPathT+"index.html";
        }else{
            localPath=localPathT;
        }
        
        log.add("url=" + uri.toString() + " localPath=" + localPath);
        
        if ("GET".equals(request.getMethod())) {
            File localFile = new File(localPath);
            if (localFile.exists()) {
                if(b.isChecked()){
                    if(getSizeFromUrl(uri)!=getSizeFromLocal(localPath)){
                        //File localFile = new File(localPath);
                        downloadAndLoad(view, uri.toString(), localFile);
                    }
                }
                return loadFromLocal(localFile);
            } else {
                downloadAndLoad(view, uri.toString(), localFile);
                return null;
            }
        }
        return null;
    }

    private WebResourceResponse loadFromLocal(File localFile) {
        String mimeType = getMimeType(localFile.getPath());
        try {
            InputStream fis = Files.newInputStream(localFile.toPath());
            return new WebResourceResponse(mimeType, "UTF-8", fis);
        } catch (IOException e) {
            log.add(e.toString());
            e.printStackTrace();
            return null;
        }
    }

    private String getMimeType(String path) {
        if (mimeTypeCache.containsKey(path)) {
            return mimeTypeCache.get(path);
        }

        try {
            String mimeType = Files.probeContentType(Paths.get(path));
            mimeTypeCache.put(path, mimeType);
            return mimeType;
        } catch (IOException e) {
            log.add(e.toString() + path);
            return null;
        }
    }

    private void downloadAndLoad(final WebView webView, final String uri, final File localFile) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(uri);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setRequestMethod("GET");

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        File parentFile = localFile.getParentFile();
                        if (parentFile != null && !parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                        try (InputStream in = new BufferedInputStream(connection.getInputStream());
                             FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                             FileChannel outputChannel = fileOutputStream.getChannel();
                             ReadableByteChannel inputChannel = Channels.newChannel(in)) {

                            outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
                        }

                        webView.post(new Runnable() {
                            @Override
                            public void run() {
                                loadFromLocal(localFile);
                                //webView.loadUrl(uri.toString());
                            }
                        });
                    } else {
                        Log.e("Download", "HTTP Error: " + connection.getResponseCode() + " for " + uri);
                        log.add("HTTP Error: " + connection.getResponseCode() + " for " + uri);
                    }
                } catch (IOException e) {
                    Log.e("Download", "Error downloading " + uri, e);
                    log.add("Download Error: " + e.toString() + uri);
                }
            }
        });
    }
}
