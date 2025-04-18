package com.sk.revisit.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.sk.revisit.MyUtils;
import com.sk.revisit.components.JSNavComponent;
import com.sk.revisit.jsconsole.JSConsoleLogger;
import com.sk.revisit.managers.WebStorageManager;


public class MyWebView extends WebView {

    MyUtils myUtils;
    MyWebViewClient.UrlLoadListener urlLoadListener;
    MyWebChromeClient.ProgressChangeListener progressChangeListener;
    JSConsoleLogger jsConsoleLogger;
    JSNavComponent jsNavComponent;


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

    public void setUrlLoadListener(MyWebViewClient.UrlLoadListener urlLoadListener) {
        this.urlLoadListener = urlLoadListener;
    }

    public void setJSNavComponent(JSNavComponent jsComponent) {
        this.jsNavComponent = jsComponent;
        this.jsConsoleLogger = jsNavComponent.getJsConsoleLogger();
    }

    public void setProgressChangeListener(MyWebChromeClient.ProgressChangeListener progressChangeListener) {
        this.progressChangeListener = progressChangeListener;
    }

    public void init() {
        WebStorageManager webStorageManager = new WebStorageManager(myUtils);
        MyWebViewClient webViewClient = new MyWebViewClient(webStorageManager);
        webViewClient.setUrlLoadListener(urlLoadListener);
        setWebViewClient(webViewClient);

        MyWebChromeClient webChromeClient = new MyWebChromeClient(jsConsoleLogger, progressChangeListener);
        webChromeClient.setProgressListener(progressChangeListener);
        setWebChromeClient(webChromeClient);

        setDownloadListener(new MyDownloadListener(getContext(), this));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initSettings() {
        WebSettings webSettings = getSettings();
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        // String webViewDBPath = myUtils.getRootPath()+"/webViewCache";
        // File webViewDBF =  new File(webViewDBPath);
        // if(webViewDBF.exists()){
        // 	webViewDBF.mkdirs();
        // }
        // webSettings.setDatabasePath(webViewDBPath);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        //webSettings.setUserAgentString();
        //setWebContentsDebuggingEnabled(false);
    }

    public void destroyWebView() {
        //clearHistory();
        //clearCache(true);
        loadUrl("about:blank");
        pauseTimers();
        removeJavascriptInterface("Revisit");
        removeAllViews();
        destroy();
    }
}
