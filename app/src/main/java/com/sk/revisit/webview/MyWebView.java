package com.sk.revisit.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.sk.revisit.MyUtils;
import com.sk.revisit.jsconsole.JSConsoleLogger;
import com.sk.revisit.managers.WebStorageManager;


public class MyWebView extends WebView {

	MyUtils myUtils;
	LinearLayout linearLayout;
	ScrollView scrollView;
	MyWebChromeClient.ProgressChangeListener progressChangeListener;


	public MyWebView(Context context) {
		super(context);
		initSettings();
	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSettings();
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initSettings();
	}

	public void setMyUtils(MyUtils myUtils) {
		this.myUtils = myUtils;
	}

	public void setLinearLayout(LinearLayout linearLayout) {
		this.linearLayout = linearLayout;
	}

	public void setScrollView(ScrollView scrollView) {
		this.scrollView = scrollView;
	}

	public void setProgressChangeListener(MyWebChromeClient.ProgressChangeListener progressChangeListener) {
		this.progressChangeListener = progressChangeListener;
	}

	public void init() {
		WebStorageManager webStorageManager = new WebStorageManager(myUtils);
		MyWebViewClient webViewClient = new MyWebViewClient(webStorageManager);
		setWebViewClient(webViewClient);

		JSConsoleLogger jsConsoleLogger = new JSConsoleLogger(getContext(), linearLayout, scrollView);
		MyWebChromeClient webChromeClient = new MyWebChromeClient(jsConsoleLogger, progressChangeListener);
		setWebChromeClient(webChromeClient);

		setDownloadListener(new MyDownloadListener(getContext()));
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initSettings() {
		WebSettings webSettings = getSettings();
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		webSettings.setUseWideViewPort(true);
		//webSettings.setUserAgentString();
		setWebContentsDebuggingEnabled(false);
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
