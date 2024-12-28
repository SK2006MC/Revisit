package com.sk.revisit.jsact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Manages WebView settings and JavaScript execution.
 */
public class JSWebViewManager {

    private static final String TAG = "JSWebViewManager";
    private final WebView webView;
    private final WebAppInterface webAppInterface;
    private final JSConsoleLogger jsLogger;

    public JSWebViewManager(Context context, WebView webView, JSConsoleLogger jsLogger) {
        this.webView = webView;
        this.jsLogger = jsLogger;
        this.webAppInterface = new WebAppInterface(context, webView);
        setupWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowContentAccess(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                jsLogger.logConsoleMessage(consoleMessage);
                return true;
            }
        });

        webView.addJavascriptInterface(webAppInterface, "Android");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public void executeJS(String jsCode, ValueCallback<String> callback) {
        webView.evaluateJavascript(jsCode, callback);
    }

    public static class WebAppInterface {
        private final Context mContext;
        WebView webview;

        WebAppInterface(Context c, WebView webview) {
            mContext = c;
            this.webview = webview;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void loadUrl(String url) {
            webview.loadUrl(url);
        }
    }
}