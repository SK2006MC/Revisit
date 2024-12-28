// JavaScriptExecutor.java
package com.sk.revisit.jsv2;

import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

/**
 * Executes JavaScript within a WebView and returns results asynchronously.
 */
public class JavaScriptExecutor {
    private static final String TAG = "JavaScriptExecutor";

    private final WebView webView;

    public JavaScriptExecutor(WebView webView) {
        this.webView = webView;
    }

    public void execute(String jsCode, ValueCallback<String> callback) {
        if (webView == null) {
            Log.e(TAG, "WebView is null, cannot execute JavaScript.");
            return;
        }

        webView.evaluateJavascript(jsCode, callback);
    }
}