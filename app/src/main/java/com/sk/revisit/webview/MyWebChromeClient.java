package com.sk.revisit.webview;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sk.revisit.jsconsole.JSConsoleLogger;

public class MyWebChromeClient extends WebChromeClient {

    final JSConsoleLogger jsLogger;
    ProgressChangeListener listener;

    public MyWebChromeClient(JSConsoleLogger jsLogger, ProgressChangeListener listener) {
        this.jsLogger = jsLogger;
        this.listener = listener;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (jsLogger != null) {
            jsLogger.logConsoleMessage(consoleMessage);
            return true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(WebView webview, int progress) {
        super.onProgressChanged(webview, progress);
        if (listener != null) {
            listener.onChange(progress);
        }
    }

    public void setProgressListener(ProgressChangeListener listener) {
        this.listener = listener;
    }

    public interface ProgressChangeListener {
        void onChange(int progress);
    }
}