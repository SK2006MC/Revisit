package com.sk.revisit.webview;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

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
}