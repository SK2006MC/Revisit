package com.sk.web6;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewUtils3 {

    public static void configureWebViewUnsafe(WebView webView) {
        WebSettings webSettings = webView.getSettings();

        // Enable JavaScript (essential for most web features)
        webSettings.setJavaScriptEnabled(true);

        // Enable DOM Storage (localStorage, sessionStorage)
        webSettings.setDomStorageEnabled(true);

        // Enable database storage API (deprecated but still used by some sites)
        
        //webSettings.setDatabaseEnabled(true);
        //String databasePath = webView.getContext().getDir("databases", Context.MODE_PRIVATE).getPath();
        //webSettings.setDatabasePath(databasePath);

        // Enable App Cache (deprecated but included for completeness - NOT RECOMMENDED)
        //webSettings.setAppCacheEnabled(true);
        //String appCachePath = webView.getContext().getDir("appcache", Context.MODE_PRIVATE).getPath();
        //webSettings.setAppCachePath(appCachePath);
        //webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Enable support for viewport meta tag
        //webSettings.setUseWideViewPort(true);
        //webSettings.setLoadWithOverviewMode(true);

        // Enable zoom controls
        //webSettings.setSupportZoom(true);
        //webSettings.setBuiltInZoomControls(true);
        //webSettings.setDisplayZoomControls(false);

        // Enable file access (EXTREMELY DANGEROUS)
        webSettings.setAllowFileAccess(true);

        // Allow mixed content (EXTREMELY DANGEROUS)
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Enable file access from file URLs (EXTREMELY DANGEROUS)
        webSettings.setAllowFileAccessFromFileURLs(true);

        // Enable universal access from file URLs (EXTREMELY DANGEROUS)
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // Enable plugins (deprecated but included for completeness - NOT RECOMMENDED)
        //webSettings.setPluginState(WebSettings.PluginState.ON);

        // Set the default text encoding
        //webSettings.setDefaultTextEncodingName("UTF-8");

        // Improve performance (hardware acceleration)
        //webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        
    }
}
