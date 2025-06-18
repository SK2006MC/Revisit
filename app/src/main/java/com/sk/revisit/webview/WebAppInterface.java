package com.sk.revisit.webview;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class WebAppInterface {
    final WebView webview;
    private final Context mContext;

    public WebAppInterface(Context c, WebView webview) {
        mContext = c;
        this.webview = webview;
    }

    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void loadUrl(String url) {
        webview.post(() -> webview.loadUrl(url));
    }

    @JavascriptInterface
    public void processBase64(String base64, String fileName, String mimeType) {
        try {
            byte[] fileData = Base64.decode(base64, Base64.DEFAULT);
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File outFile = new File(downloadsDir, fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(fileData);
            fos.close();
            Toast.makeText(mContext, "Blob file saved: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "Failed to save blob: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}