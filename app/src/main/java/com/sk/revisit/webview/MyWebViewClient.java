package com.sk.revisit.webview;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sk.revisit.managers.WebStorageManager;

public class MyWebViewClient extends WebViewClient {

    WebStorageManager webStorageManager;

    public MyWebViewClient(Context context) {
        webStorageManager = new WebStorageManager(context);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return webStorageManager.getStoredResponse(request);
    }
}