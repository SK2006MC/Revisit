package com.sk.revisit.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class MyWebView extends WebView {


	public MyWebView(Context context) {
		super(context);
		init(context);
	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void init(Context context) {
		WebSettings webSettings = getSettings();

//		MyWebViewClient client = new MyWebViewClient(new WebStorageManager(myUtils));
//		client.setUrlLoadListener(url -> runOnUiThread(() -> urlEditText.setText(url)));

		setDownloadListener(new MyDownloadListener(context));
//		setWebViewClient(client);

		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setJavaScriptEnabled(true);
		// should remove this line in production
		webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		webSettings.setUseWideViewPort(true);
		// webSettings.setUserAgentString(); // Consider setting a custom User-Agent if needed

		// Enable remote debugging
//		setWebContentsDebuggingEnabled(true);
	}

	public void destroyWebView() {
		clearHistory();
		clearCache(true);
		loadUrl("about:blank");
		pauseTimers();
		removeJavascriptInterface("Revisit");
		removeAllViews();
		destroy();
	}
}
