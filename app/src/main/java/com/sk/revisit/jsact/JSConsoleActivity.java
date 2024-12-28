package com.sk.revisit.jsact;

import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;

import com.sk.revisit.R;
import com.sk.revisit.jsv2.JSAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JSConsoleActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final Pattern QUOTE_REMOVAL_PATTERN = Pattern.compile("^\"|\"$");
    private static final String JS_INTERFACE_NAME = "Android";
    private final List<String> jsCodeHistory = new ArrayList<>();
    private WebView webView;
    private DrawerLayout drawerLayout;
    private LinearLayout consoleLayout;
    private ScrollView consoleScrollView;
    private JSAutoCompleteTextView jsInput;
    private Button executeJsButton;
    private JSWebViewManager jsWebViewManager;
    private JSConsoleLogger jsLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        jsWebViewManager = new JSWebViewManager(this, webView, jsLogger);
        jsLogger = new JSConsoleLogger(this, consoleLayout, consoleScrollView);
        jsInput.setWebView(webView);

        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                if (webView != null) {
                    webView.loadUrl("about:blank");
                    webView.clearHistory();
                    webView.removeAllViews();
                    ((ViewGroup) webView.getParent()).removeView(webView);
                    webView.destroy();
                }
            }
        });
    }

    private void initializeViews() {
        webView = findViewById(R.id.myWebView);
    /*drawerLayout = findViewById(R.id.);
    consoleLayout = findViewById(R.id.myWebView);
    consoleScrollView = findViewById(R.id.myWebView);
    jsInput = findViewById(R.id.myWebView);
    executeJsButton = findViewById(R.id.myWebView);*/
    }

    private void setupExecuteButton() {
    /*executeJsButton.setOnClickListener(
        v -> {
          String jsCode = jsInput.getText().toString().trim();
          if (!jsCode.isEmpty()) {
            jsCodeHistory.add(jsCode);
            jsInput.adapter.notifyDataSetChanged();
            executeJS(jsCode);
            jsInput.setText("");
          }
        });*/
        executeJsButton.setOnClickListener(v -> executeUserJS());
        executeJsButton.setOnLongClickListener(
                arg0 -> {
                    consoleLayout.removeAllViewsInLayout();
                    return false;
                });
    }

    private void executeUserJS() {
        String jsCode = jsInput.getText().toString().trim();
        if (!jsCode.isEmpty()) {
            jsCodeHistory.add(jsCode);
            jsInput.adapter.notifyDataSetChanged();
            jsWebViewManager.executeJS(jsCode, result -> {
                String formattedResult = result.replaceAll("^\"|\"$", "");
                jsLogger.logConsoleMessage(">" + jsCode + '\n' + "Result: " + formattedResult, ConsoleMessage.MessageLevel.LOG);
            });
            jsInput.setText("");
        }
    }
}
