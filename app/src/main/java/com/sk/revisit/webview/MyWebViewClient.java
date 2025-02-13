package com.sk.revisit.webview;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sk.revisit.managers.WebStorageManager;

public class MyWebViewClient extends WebViewClient {
	private final WebStorageManager webStorageManager;
	private UrlLoadListener listener;

	public MyWebViewClient(WebStorageManager webStorageManager) {
		this.webStorageManager = webStorageManager;
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
		return webStorageManager.getResponse(request);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
		Uri url = request.getUrl();
		if (listener != null) {
			listener.load(url.toString());
		}
		return true;
	}

	public void setUrlLoadListener(UrlLoadListener listener) {
		this.listener = listener;
	}

	@SuppressLint("WebViewClientOnReceivedSslError")
	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		handler.proceed();
	}

	public interface UrlLoadListener {
		void load(String url);
	}
}