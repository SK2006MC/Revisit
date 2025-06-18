package com.sk.revisit.webview;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.Toast;

public class MyDownloadListener implements DownloadListener {
    private Context context;
    private WebView webView;

    public MyDownloadListener(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (url.startsWith("blob:")) {
            // Handle blob URLs via JS interface
            String jsCode =
                    "var xhr = new XMLHttpRequest();" +
                            "xhr.open('GET', '" + url + "', true);" +
                            "xhr.responseType = 'blob';" +
                            "xhr.onload = function() {" +
                            "  var reader = new FileReader();" +
                            "  reader.onloadend = function() {" +
                            "    var base64data = reader.result.split(',')[1];" +
                            "    window.AndroidBlobDownloader.processBase64(base64data, '" +
                            URLUtil.guessFileName(url, contentDisposition, mimetype) + "', '" + mimetype + "');" +
                            "  };" +
                            "  reader.readAsDataURL(xhr.response);" +
                            "};" +
                            "xhr.send();";
            webView.evaluateJavascript(jsCode, null);
            Toast.makeText(context, "Processing blob download...", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);
        request.setDescription("Downloading file...");
        request.setMimeType(mimetype);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }
}