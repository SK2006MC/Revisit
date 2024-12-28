package com.sk.revisit.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.sk.revisit.R;
import com.sk.revisit.jsact.JSConsoleLogger;
import com.sk.revisit.jsact.JSWebViewManager;
import com.sk.revisit.jsv2.JSAutoCompleteTextView;
import com.sk.revisit.webview.MyWebViewClient;

public class MainActivity extends AppCompatActivity {

    EditText url;
    WebView webView;
    NavigationView navigationView;
    LinearLayout jsNav;
    JSConsoleLogger jsLogger;
    JSWebViewManager jsManager;
    View headerView;
    ScrollView jsScrollView;
    LinearLayout jsLinearLayout;
    DrawerLayout drawerLayout;
    JSAutoCompleteTextView jsTextView;
    ImageButton jsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.myNav);
        headerView = navigationView.getHeaderView(0);
        url = headerView.findViewById(R.id.urlEditText);
        webView = findViewById(R.id.myWebView);

        initJSConsole();
        initNavView(navigationView);
        initWebView(webView);
        initUrlEditText(url, webView);

        jsTextView.setWebView(webView);
    }

    void initJSConsole() {
        jsNav = findViewById(R.id.jsnav);
        jsTextView = findViewById(R.id.js_input);
        jsLinearLayout = findViewById(R.id.console_layout);
        jsScrollView = findViewById(R.id.console_scroll_view);
        jsLogger = new JSConsoleLogger(this, jsLinearLayout, jsScrollView);
        jsButton = findViewById(R.id.execute_js_btn);
        jsManager = new JSWebViewManager(this, webView, jsLogger);

        jsButton.setOnClickListener(v -> {
            String code = jsTextView.getText().toString();
            jsManager.executeJS(code, r -> jsLogger.logConsoleMessage(r));
        });

    }

    void initNavView(@NonNull NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dn) {
                startMyActivity(DownloadActivity.class);
            } else if (id == R.id.nav_ud) {
                startMyActivity(UpdateActivity.class);
            } else if (id == R.id.nav_settings) {
                startMyActivity(SettingsActivity.class);
            } else if (id == R.id.nav_about) {
                startMyActivity(AboutActivity.class);
            } else if (id == R.id.nav_web) {
                startMyActivity(WebpagesActivity.class);
            }
            return false;
        });
    }

    void initUrlEditText(@NonNull EditText url, WebView webView) {
        url.setOnFocusChangeListener((a, b) -> {
            try {
                webView.loadUrl(url.getText().toString());
            } catch (Exception e) {
                alert(e.toString());
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    void initWebView(@NonNull WebView wv) {
        wv.setWebViewClient(new MyWebViewClient(this));
        WebSettings set = wv.getSettings();
        set.setAllowContentAccess(true);
        set.setAllowFileAccess(true);
        set.setAllowFileAccessFromFileURLs(true);
        set.setAllowUniversalAccessFromFileURLs(true);
        set.setDatabaseEnabled(true);
//        set.setDatabasePath(getExternalFilesDir(null).getAbsolutePath());
        set.setDomStorageEnabled(true);
        set.setJavaScriptCanOpenWindowsAutomatically(true);
        set.setJavaScriptEnabled(true);
        set.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        set.setOffscreenPreRaster(true);
        set.setUseWideViewPort(true);
//        set.setUserAgentString();
    }

    @Override
    public void onBackPressed() {
        try {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else if (drawerLayout.isDrawerOpen(R.id.jsnav)) {
                drawerLayout.closeDrawer(R.id.jsnav);
            } else if (webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            alert(e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void startMyActivity(Class c) {
        startActivity(new Intent(this, c));
    }

    void alert(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}