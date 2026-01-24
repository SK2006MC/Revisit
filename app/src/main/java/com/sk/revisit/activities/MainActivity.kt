package com.sk.revisit.activities

import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.sk.revisit.MyUtils
import com.sk.revisit.R
import com.sk.revisit.Revisit
import com.sk.revisit.Consts
import com.sk.revisit.components.JSNavComponent
import com.sk.revisit.components.UrlBarComponent
import com.sk.revisit.databinding.ActivityMainBinding
import com.sk.revisit.databinding.NavHeaderBinding
import com.sk.revisit.helper.NavigationHelper
import com.sk.revisit.helper.NetworkHelper
import com.sk.revisit.managers.MySettingsManager
import com.sk.revisit.webview.MyWebView
import java.util.*

class MainActivity : BaseActivity() {

    // Using nullable types for UI components to avoid UninitializedPropertyAccessException
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var mainWebView: MyWebView? = null
    private var urlBarComponent: UrlBarComponent? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Check early exit before doing any UI work
        val settings = revisitApp.mySettingsManager
        if (settings?.isFirst == true) {
            startMyActivity<FirstActivity>(fini = true)
            return
        }

        // 2. Setup View Binding
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Initialize Components
        setupDependencies()
        initializeUI()
        setupNetworkMonitoring()
        setupBackNavigation()
    }

    private fun setupDependencies() {
        // Safe access to app-level utilities
        val myUtils = revisitApp.myUtils ?: return
        
        mainWebView = binding.myWebView
        mainWebView?.apply {
            setMyUtils(myUtils)
            init()
        }
    }

    private fun initializeUI() {
        val webView = mainWebView ?: return
        val headerView = binding.myNav.getHeaderView(0)
        val navHeaderBinding = NavHeaderBinding.bind(headerView)

        // Setup UrlBarComponent
        urlBarComponent = UrlBarComponent(
            this,
            navHeaderBinding.urlAppCompatAutoCompleteTextView,
            webView,
            revisitApp.mySettingsManager?.rootStoragePath ?: ""
        )

        // Setup Navigation Helper
        NavigationHelper.setupNavigation(this, binding, webView, revisitApp)
        
        // Setup JS Navigation
        JSNavComponent(this, binding.jsnav, webView)

        // UI Listeners
        setupHeaderListeners(navHeaderBinding)
    }

    private fun setupHeaderListeners(header: NavHeaderBinding) {
        header.urlLogs.setOnClickListener {
            header.urlLogs.text = String.format(
                Locale.ENGLISH,
                "Req: %d | Res: %d | Fail: %d",
                Revisit.requests.get(), Revisit.resolved.get(), Revisit.failed.get()
            )
        }

        header.useInternet.setOnCheckedChangeListener { _, isChecked ->
            Revisit.isNetworkAvailable = isChecked
            header.keepUptodate.isEnabled = isChecked
        }
    }

    private fun setupNetworkMonitoring() {
        networkCallback = NetworkHelper.registerNetworkCallback(this) { isAvailable ->
            runOnUiThread { updateNetworkUI(isAvailable) }
        }
    }

    private fun updateNetworkUI(isAvailable: Boolean) {
        val headerView = binding.myNav.getHeaderView(0)
        val navHeaderBinding = NavHeaderBinding.bind(headerView)
        val color = ContextCompat.getColor(
            this, 
            if (isAvailable) R.color.dark_teal_200 else R.color.black
        )
        navHeaderBinding.background.setBackgroundColor(color)
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val drawer = binding.drawerLayout
                val webView = mainWebView

                when {
                    drawer.isDrawerOpen(binding.myNav) -> drawer.closeDrawer(binding.myNav)
                    drawer.isDrawerOpen(binding.nav2) -> drawer.closeDrawer(binding.nav2)
                    webView?.canGoBack() == true -> webView.goBack()
                    else -> handleDoubleBackExit()
                }
            }
        })
    }

    private fun handleDoubleBackExit() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < BACK_PRESS_INTERVAL) {
            finish()
        } else {
            lastBackPressTime = currentTime
            alert("Press again to exit")
        }
    }

    override fun onDestroy() {
        // Safely release components only if they were created
        urlBarComponent?.release()
        mainWebView?.destroyWebView()
        
        networkCallback?.let {
            NetworkHelper.unregisterNetworkCallback(this, it)
        }
        
        _binding = null // Prevent memory leaks
        super.onDestroy()
    }

    companion object {
        private var lastBackPressTime: Long = 0L
        private const val BACK_PRESS_INTERVAL = 2000L
    }
}
