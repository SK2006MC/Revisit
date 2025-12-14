package com.sk.revisit.helper;

import android.util.Log;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.sk.revisit.Revisit;
import com.sk.revisit.activities.MainActivity;
import com.sk.revisit.activities.UpdateActivity;
import com.sk.revisit.activities.WebpagesActivity;
import com.sk.revisit.activities.DownloadActivity;
import com.sk.revisit.activities.SettingsActivity;
import com.sk.revisit.activities.AboutActivity;
import com.sk.revisit.activities.LogActivity;
import com.sk.revisit.activities.UtilsActivity;
import com.sk.revisit.components.JSNavComponent;
import com.sk.revisit.databinding.ActivityMainBinding;
import com.sk.revisit.webview.MyWebView;

/**
 * NavigationHelper
 *
 * Extracts navigation drawer wiring from MainActivity to keep the activity focused on lifecycle and UI wiring.
 * It assumes the passed activity is MainActivity (so it can call startMyActivity).
 */
public final class NavigationHelper {

    private static final String TAG = "NavigationHelper";

    private NavigationHelper() { /* no-op */ }

    public static void setupNavigation(MainActivity activity, ActivityMainBinding binding, MyWebView mainWebView, Revisit revisitApp) {
        if (activity == null || binding == null) {
            Log.w(TAG, "setupNavigation: activity or binding is null");
            return;
        }

        binding.myNav.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == com.sk.revisit.R.id.nav_dn) {
                activity.startMyActivity(DownloadActivity.class);
            } else if (id == com.sk.revisit.R.id.nav_ud) {
                activity.startMyActivity(UpdateActivity.class);
            } else if (id == com.sk.revisit.R.id.nav_settings) {
                activity.startMyActivity(SettingsActivity.class, true);
            } else if (id == com.sk.revisit.R.id.nav_about) {
                activity.startMyActivity(AboutActivity.class);
            } else if (id == com.sk.revisit.R.id.nav_web) {
                activity.startMyActivity(WebpagesActivity.class);
                if (revisitApp != null) revisitApp.setLastActivity(activity);
            } else if (id == com.sk.revisit.R.id.nav_logs) {
                activity.startMyActivity(LogActivity.class);
            } else if (id == com.sk.revisit.R.id.nav_utils) {
                activity.startMyActivity(UtilsActivity.class);
            } else if (id == com.sk.revisit.R.id.refresh) {
                if (mainWebView != null) mainWebView.reload();
            }
            // indicate selection handled
            return true;
        });

        // Optionally, additional setup for secondary nav (jsNav) can be added here.
    }
}