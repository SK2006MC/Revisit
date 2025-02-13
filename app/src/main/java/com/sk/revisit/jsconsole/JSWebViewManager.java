package com.sk.revisit.jsconsole;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.sk.revisit.webview.MyWebChromeClient;
import com.sk.revisit.webview.WebAppInterface;

/**
 * Manages WebView settings and JavaScript execution.
 */
public class JSWebViewManager {

	private final WebView webView;
	private final WebAppInterface webAppInterface;
	private final JSConsoleLogger jsLogger;

	public JSWebViewManager(Context context, WebView webView, JSConsoleLogger jsLogger) {
		this.webView = webView;
		this.jsLogger = jsLogger;
		this.webAppInterface = new WebAppInterface(context, webView);
		setupWebView();
	}

	@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
	private void setupWebView() {
		MyWebChromeClient chromeClient = new MyWebChromeClient(jsLogger, null);
		webView.setWebChromeClient(chromeClient);
		webView.addJavascriptInterface(webAppInterface, "Revisit");
		webView.loadUrl("file:///android_asset/index.html");
	}

	public void executeJS(String jsCode, ValueCallback<String> callback) {
		webView.evaluateJavascript(jsCode, callback);
	}
}