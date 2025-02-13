package com.sk.revisit.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.sk.revisit.adapter.UrlsLogAdapter;
import com.sk.revisit.data.UrlLog;
import com.sk.revisit.jsconsole.JSAutoCompleteTextView;
import com.sk.revisit.jsconsole.JSConsoleLogger;
import com.sk.revisit.jsconsole.JSWebViewManager;
import com.sk.revisit.log.Log;
import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.databinding.ActivityMainBinding;
import com.sk.revisit.databinding.NavHeaderBinding;
import com.sk.revisit.databinding.NavJsBinding;
import com.sk.revisit.managers.MySettingsManager;
import com.sk.revisit.managers.WebStorageManager;
import com.sk.revisit.webview.MyDownloadListener;
import com.sk.revisit.webview.MyWebChromeClient;
import com.sk.revisit.webview.MyWebViewClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";
	final String fm = "requests %d\nresolved %d\nfailed %d";
	TextView inf;
	MySettingsManager settingsManager;
	private EditText urlEditText;
	private WebView mainWebView;
	private ScrollView jsConsoleScrollView;
	private LinearLayout jsConsoleLayout;
	private LinearLayout bg;
	private JSAutoCompleteTextView jsInputTextView;
	private ImageButton executeJsButton;
	@SuppressLint("UseSwitchCompatOrMaterialCode")
	private SwitchCompat su;
	private JSConsoleLogger jsConsoleLogger;
	private JSWebViewManager jsWebViewManager;
	private MyUtils myUtils;
	private ActivityMainBinding binding;
	private ConnectivityManager.NetworkCallback networkCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settingsManager = new MySettingsManager(this);

		if (settingsManager.getIsFirst()) {
			startMyActivity(FirstActivity.class);
		}

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		// Initialize UI elements using binding
		initializeUI();

		// Initialize Managers and Utilities
		myUtils = new MyUtils(this, settingsManager.getRootStoragePath());
		myUtils.log(TAG, "hi");
		jsConsoleLogger = new JSConsoleLogger(this, jsConsoleLayout, jsConsoleScrollView);
		jsWebViewManager = new JSWebViewManager(this, mainWebView, jsConsoleLogger);

		// Initialize components
		initNetworkChangeListener();
		initJSConsole();
		initNavView();
		initWebView(mainWebView);
		initUrlEditText(urlEditText, mainWebView);

		jsInputTextView.setWebView(mainWebView);

		su.setOnCheckedChangeListener((v, c) -> MyUtils.shouldUpdate = c);
		initProgressBar(mainWebView);
	}

	void initProgressBar(WebView webView) {
		MyWebChromeClient chromeClient = (MyWebChromeClient) webView.getWebChromeClient();

		if (chromeClient != null) {
			chromeClient.setProgressListener(progress -> binding.pageLoad.setProgress(progress));
		}
	}

	@Override 
	protected void onResume() {
		super.onResume();
		if (getIntent().getBooleanExtra("loadUrl", false)) {
			String url = getIntent().getStringExtra("url");
			if (url != null) {
				mainWebView.loadUrl(url);
				urlEditText.setText(url);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myUtils.shutdown();

		saveLog(Log.getLogs());

		// Unregister network callback to prevent memory leaks
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (networkCallback != null) {
			connectivityManager.unregisterNetworkCallback(networkCallback);
		}
	}

	void saveLog(List<String[]> logs){
		try{
			File logFile = new File(settingsManager.getRootStoragePath()+"/log2.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			for(String[] log:logs){
				writer.write(Arrays.toString(log));
				writer.newLine();
				writer.flush();
			}
			writer.close();
		}catch (Exception e){
			showAlert(e.toString());
		}
	}

	private void initializeUI() {
		NavHeaderBinding navHeaderBinding = NavHeaderBinding.bind(binding.myNav.getHeaderView(0));
		urlEditText = navHeaderBinding.urlEditText;
		su = navHeaderBinding.su;
		bg = navHeaderBinding.bg;
		inf = navHeaderBinding.inf;
		inf.setOnClickListener((v) -> inf.setText(String.format(Locale.ENGLISH,fm, MyUtils.requests.get(), MyUtils.resolved.get(), MyUtils.failed.get())));

		mainWebView = binding.myWebView;

		NavJsBinding jsConsole = binding.jsnav;
		jsInputTextView = jsConsole.jsInput;
		jsConsoleLayout = jsConsole.consoleLayout;
		jsConsoleScrollView = jsConsole.consoleScrollView;
		executeJsButton = jsConsole.executeJsBtn;
	}

	private void initNetworkChangeListener() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkRequest request = new NetworkRequest.Builder()
				.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
				.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.build();

		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(@NonNull Network network) {
				super.onAvailable(network);
				Log.d(TAG, "Network Available");
				MyUtils.isNetworkAvailable = true;
				changeBgColor(true);
			}

			@Override
			public void onLost(@NonNull Network network) {
				super.onLost(network);
				Log.d(TAG, "Network Lost");
				MyUtils.isNetworkAvailable = false;
				changeBgColor(false);
			}
		};
		connectivityManager.registerNetworkCallback(request, networkCallback);
	}

	public void changeBgColor(boolean isAvailable) {
		runOnUiThread(() -> {
			if (isAvailable) {
				bg.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
			} else {
				bg.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700));
			}
		});
	}

	private void initJSConsole() {
		executeJsButton.setOnClickListener(v -> {
			String code = jsInputTextView.getText().toString();
			jsWebViewManager.executeJS(code, r -> jsConsoleLogger.logConsoleMessage(">" + code + "\n" + r + "\n"));
		});

		executeJsButton.setOnLongClickListener(arg0 -> {
			jsConsoleLayout.removeAllViewsInLayout();
			return true;
		});
	}

	private void initNavView() {
		binding.myNav.setNavigationItemSelectedListener(item -> {

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
			} else if (id == R.id.nav_logs) {
				startMyActivity(LogActivity.class);
			} else if (id == R.id.nav_utils) {
				startMyActivity(UtilsActivity.class);
			} else if (id == R.id.refresh) {
				mainWebView.reload();
			}
			// Return true to indicate that the item selection is handled
			return true;
		});
	}

	private void initUrlEditText(@NonNull EditText urlEditText, WebView webView) {
		urlEditText.setOnFocusChangeListener((view, hasFocus) -> {
			if (hasFocus) return; // Only load URL when focus is lost
			try {
				webView.loadUrl(urlEditText.getText().toString());
			} catch (Exception e) {
				showAlert(e.toString());
			}
		});

		urlEditText.setOnEditorActionListener((v, actionId, event) -> {
			try {
				mainWebView.loadUrl(urlEditText.getText().toString());
			} catch (Exception e) {
				showAlert(e.toString());
			}
			return true;
		});
	}


	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(@NonNull WebView webView) {

		MyWebViewClient client = new MyWebViewClient(new WebStorageManager(myUtils));
		client.setUrlLoadListener(url -> runOnUiThread(() -> urlEditText.setText(url)));

		webView.setDownloadListener(new MyDownloadListener(this));

		webView.setWebViewClient(client);
		WebSettings webSettings = webView.getSettings();
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		webSettings.setUseWideViewPort(true);
		// webSettings.setUserAgentString(); // Consider setting a custom User-Agent if needed
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawerLayout = binding.drawerLayout;
		NavigationView mainNav = binding.myNav;
		LinearLayout jsl = binding.jsnav.getRoot();
		try {
			if (drawerLayout.isDrawerOpen(mainNav)) {
				drawerLayout.closeDrawer(mainNav);
			} else if (drawerLayout.isDrawerOpen(jsl)) {
				drawerLayout.closeDrawer(jsl);
			} else if (mainWebView.canGoBack()) {
				mainWebView.goBack();
			} else {
				super.onBackPressed();
			}
		} catch (Exception e) {
			showAlert(e.toString());
		}
	}

	private void startMyActivity(Class<?> activityClass) {
		startActivity(new Intent(this, activityClass));
	}

	private void showAlert(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}