package com.sk.revisit.activities

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.sk.revisit.MyUtils
import com.sk.revisit.R
import com.sk.revisit.Revisit
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

    private val urlLogsFormat = "Requested: %d\nResolved: %d\nFailed: %d"
    private var jsNavComponent: JSNavComponent? = null
    private var urlBarComponent: UrlBarComponent? = null
    private var revisitApp: Revisit? = null
    private var urlLogsTextView: TextView? = null
    private var settingsManager: MySettingsManager? = null
    private var mainWebView: MyWebView? = null
    private var backgroundLinearLayout: LinearLayout? = null
    private var keepUpToDateSwitch: SwitchCompat? = null
    private var myUtils: MyUtils? = null
    private lateinit var binding: ActivityMainBinding
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        revisitApp = getRevisitApp()
        myUtils = revisitApp?.myUtils
        settingsManager = revisitApp?.mySettingsManager

        if (settingsManager?.isFirst == true) {
            startMyActivity(FirstActivity::class.java)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeUI()
        networkCallback = NetworkHelper.registerNetworkCallback(this) { isAvailable ->
            Log.d(TAG, "Network state changed: $isAvailable")
            changeBgColor(isAvailable)
        }
        NavigationHelper.setupNavigation(this, binding, mainWebView, revisitApp)

        mainWebView?.let { initWebView(it) }
        initOnBackPressed()
    }

    private fun initializeUI() {
        mainWebView = binding.myWebView

        val navHeaderBinding = NavHeaderBinding.bind(binding.myNav.getHeaderView(0))
        keepUpToDateSwitch = navHeaderBinding.keepUptodate
        backgroundLinearLayout = navHeaderBinding.background
        urlLogsTextView = navHeaderBinding.urlLogs
        urlLogsTextView?.setOnClickListener {
            urlLogsTextView?.text = String.format(
                Locale.ENGLISH, urlLogsFormat,
                Revisit.requests.get(), Revisit.resolved.get(), Revisit.failed.get()
            )
        }

        val useInternetSwitch = navHeaderBinding.useInternet
        useInternetSwitch.setOnCheckedChangeListener { _, isChecked ->
            Revisit.isNetworkAvailable = isChecked
            keepUpToDateSwitch?.isEnabled = isChecked
        }

        keepUpToDateSwitch?.setOnCheckedChangeListener { _, isChecked ->
            Revisit.shouldUpdate = isChecked
        }

        urlBarComponent = UrlBarComponent(
            this,
            navHeaderBinding.urlAppCompatAutoCompleteTextView,
            mainWebView!!,
            settingsManager!!.rootStoragePath
        )
        jsNavComponent = JSNavComponent(this, binding.jsnav, mainWebView)
    }

    private fun initWebView(webView: MyWebView) {
        webView.setMyUtils(myUtils)
        webView.setJSNavComponent(jsNavComponent)
        webView.setUrlLoadListener { url ->
            urlBarComponent?.setText(url)
            urlLogsTextView?.performClick()
        }

        webView.setProgressChangeListener { progress ->
            binding.pageLoad.progress = progress
            if (progress == 100)
                binding.pageLoad.visibility = View.GONE
            else
                binding.pageLoad.visibility = View.VISIBLE
        }

        webView.init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_men2, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.edit_html) {
            //
        } else if (id == R.id.fullscreen) {
            // fullscreen toggle omitted
        } else {
            return false
        }
        return true
    }

    fun changeBgColor(isAvailable: Boolean) {
        runOnUiThread {
            if (isAvailable) {
                backgroundLinearLayout?.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.dark_teal_200)
                )
            } else {
                backgroundLinearLayout?.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.black)
                )
            }
        }
    }

    fun initOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val drawerLayout: DrawerLayout = binding.drawerLayout
                val mainNav: NavigationView = binding.myNav
                val jsNav: NavigationView = binding.nav2
                try {
                    if (drawerLayout.isDrawerOpen(mainNav)) {
                        drawerLayout.closeDrawer(mainNav)
                    } else if (drawerLayout.isDrawerOpen(jsNav)) {
                        drawerLayout.closeDrawer(jsNav)
                    } else if (mainWebView?.canGoBack() == true) {
                        mainWebView?.goBack()
                    } else {
                        if (bpn > 0) {
                            finish()
                        }
                        alert("press again to exit")
                        bpn++
                    }
                } catch (e: Exception) {
                    alert(e.toString())
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("loadUrl", false)) {
            val url = intent.getStringExtra("url")
            if (url != null) {
                mainWebView?.loadUrl(url)
                urlBarComponent?.setText(url)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainWebView?.destroyWebView()
        networkCallback?.let {
            NetworkHelper.unregisterNetworkCallback(this, it)
            networkCallback = null
        }
    }

    companion object {
        @JvmField
        var bpn: Int = 0
    }
}
