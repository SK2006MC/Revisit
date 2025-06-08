package com.sk.revisit.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.Revisit;
import com.sk.revisit.components.JSNavComponent;
import com.sk.revisit.components.UrlBarComponent;
import com.sk.revisit.databinding.ActivityMainBinding;
import com.sk.revisit.databinding.NavHeaderBinding;
import com.sk.revisit.log.Log;
import com.sk.revisit.managers.MySettingsManager;
import com.sk.revisit.webview.MyWebView;

import java.io.File;
import java.util.Locale;

public class MainActivity extends BaseActivity {

	final String urlLogsFormat = "Requested: %d\nResolved: %d\nFailed: %d";
	JSNavComponent jsNavComponent;
	UrlBarComponent urlBarComponent;
	Revisit revisitApp;
	private TextView urlLogsTextView;
	private MySettingsManager settingsManager;
	private MyWebView mainWebView;
	private LinearLayout backgroundLinearLayout;
	private SwitchCompat keepUpToDateSwitch;
	private MyUtils myUtils;
	private ActivityMainBinding binding;
	private ConnectivityManager.NetworkCallback networkCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		revisitApp = getRevisitApp();
		myUtils = revisitApp.getMyUtils();
		settingsManager = revisitApp.getMySettingsManager();

		if (settingsManager.getIsFirst()) {
			startMyActivity(FirstActivity.class);
			finish();
		}

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		initializeUI();
		initNetworkChangeListener();
		initNavView();
		initWebView(mainWebView);

		initOnBackPressed();
	}

	private void initializeUI() {
		mainWebView = binding.myWebView;

		NavHeaderBinding navHeaderBinding = NavHeaderBinding.bind(binding.myNav.getHeaderView(0));
		keepUpToDateSwitch = navHeaderBinding.keepUptodate;
		backgroundLinearLayout = navHeaderBinding.background;
		urlLogsTextView = navHeaderBinding.urlLogs;
		urlLogsTextView.setOnClickListener((v) -> urlLogsTextView.setText(String.format(Locale.ENGLISH, urlLogsFormat, MyUtils.requests.get(), MyUtils.resolved.get(), MyUtils.failed.get())));

		SwitchCompat useInternetSwitch = navHeaderBinding.useInternet;
		useInternetSwitch.setOnCheckedChangeListener((b, s) -> {
			MyUtils.isNetworkAvailable = s;
			keepUpToDateSwitch.setEnabled(s);
		});

		keepUpToDateSwitch.setOnCheckedChangeListener((v, c) -> MyUtils.shouldUpdate = c);

		urlBarComponent = new UrlBarComponent(this, navHeaderBinding.urlEditText, mainWebView);
		jsNavComponent = new JSNavComponent(this, binding.jsnav, mainWebView);
	}

	private void initWebView(@NonNull MyWebView webView) {
		webView.setMyUtils(myUtils);
		webView.setJSNavComponent(jsNavComponent);
		webView.setUrlLoadListener(url -> urlBarComponent.setText(url));

		webView.setProgressChangeListener(progress -> {
			binding.pageLoad.setProgress(progress);
			if(progress==100)
				binding.pageLoad.setVisibility(View.GONE);
			else if(progress==0)
				binding.pageLoad.setVisibility(View.VISIBLE);

		});

		webView.init();
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
				backgroundLinearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
			} else {
				backgroundLinearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700));
			}
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
				startMyActivity(WebpagesActivity.class, true);
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

	public void initOnBackPressed() {
		getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
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
						finish();
					}
				} catch (Exception e) {
					alert(e.toString());
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getIntent().getBooleanExtra("loadUrl", false)) {
			String url = getIntent().getStringExtra("url");
			if (url != null) {
				mainWebView.loadUrl(url);
				urlBarComponent.setText(url);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mainWebView.destroyWebView();

		File saveLogPath = new File(settingsManager.getRootStoragePath() + "/log2.txt");
		try {
			Log.saveLog(saveLogPath);
		} catch (Exception e) {
			alert(e.toString());
		}

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (networkCallback != null) {
			connectivityManager.unregisterNetworkCallback(networkCallback);
		}
	}
}