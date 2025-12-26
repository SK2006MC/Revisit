package com.sk.revisit.helper

import android.util.Log
import com.sk.revisit.R
import com.sk.revisit.Revisit
import com.sk.revisit.activities.*
import com.sk.revisit.databinding.ActivityMainBinding
import com.sk.revisit.webview.MyWebView

/**
 * NavigationHelper
 *
 * Extracts navigation drawer wiring from MainActivity to keep the activity focused on lifecycle.
 */
object NavigationHelper {

    private const val TAG = "NavigationHelper"

    fun setupNavigation(
        activity: MainActivity,
        binding: ActivityMainBinding,
        mainWebView: MyWebView,
        revisitApp: Revisit
    ) {
        binding.myNav.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dn -> activity.startMyActivity<DownloadActivity>()

                R.id.nav_ud -> activity.startMyActivity<UpdateActivity>()

                R.id.nav_settings -> activity.startMyActivity<SettingsActivity>(fini = true)

                R.id.nav_about -> activity.startMyActivity<AboutActivity>()

                R.id.nav_web -> {
                    activity.startMyActivity<WebpagesActivity>()
                    revisitApp.lastActivity = activity
                }

                R.id.nav_logs -> activity.startMyActivity<LogActivity>()

                R.id.nav_utils -> activity.startMyActivity<UtilsActivity>()

                R.id.refresh -> mainWebView.reload()

                else -> {
                    Log.w(TAG, "Unknown navigation item clicked: ${item.itemId}")
                    return@setNavigationItemSelectedListener false
                }
            }

            // Automatically close the drawer after a selection is made
            binding.drawerLayout.closeDrawer(binding.myNav)
            true
        }
    }
}
