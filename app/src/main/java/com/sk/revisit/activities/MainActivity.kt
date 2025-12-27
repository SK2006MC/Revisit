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

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsManager: MySettingsManager
    private lateinit var mainWebView: MyWebView
    private lateinit var myUtils: MyUtils

    private var jsNavComponent: JSNavComponent? = null
    private var urlBarComponent: UrlBarComponent? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Using the 'revisitApp' property inherited from BaseActivity
        // Added '!!' to resolve the Type Mismatch error
        myUtils = revisitApp.myUtils!!
        settingsManager = revisitApp.mySettingsManager!!

        // Check first run
        if (settingsManager.isFirst) {
            startMyActivity<FirstActivity>(fini = true)
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
        initWebView()
        initOnBackPressed()
    }

    private fun initializeUI() {
        mainWebView = binding.myWebView
        val navHeaderBinding = NavHeaderBinding.bind(binding.myNav.getHeaderView(0))

        with(navHeaderBinding) {
            urlLogs.setOnClickListener {
                urlLogs.text = String.format(
                    Locale.ENGLISH,
                    "Requested: %d\nResolved: %d\nFailed: %d",
                    Revisit.requests.get(), Revisit.resolved.get(), Revisit.failed.get()
                )
            }

            useInternet.setOnCheckedChangeListener { _, isChecked ->
                Revisit.isNetworkAvailable = isChecked
                keepUptodate.isEnabled = isChecked
            }

            keepUptodate.setOnCheckedChangeListener { _, isChecked ->
                Revisit.shouldUpdate = isChecked
            }

            urlBarComponent = UrlBarComponent(
                this@MainActivity,
                urlAppCompatAutoCompleteTextView,
                mainWebView,
                settingsManager.rootStoragePath
            )
        }

        jsNavComponent = JSNavComponent(this, binding.jsnav, mainWebView)
    }

    private fun initWebView() {
        mainWebView.apply {
            setMyUtils(myUtils)
            setJSNavComponent(jsNavComponent)

            setUrlLoadListener { url ->
                urlBarComponent?.setText(url)
                val navHeaderBinding = NavHeaderBinding.bind(binding.myNav.getHeaderView(0))
                navHeaderBinding.urlLogs.performClick()
            }

            setProgressChangeListener { progress ->
                binding.pageLoad.apply {
                    this.progress = progress
                    visibility = if (progress == 100) View.GONE else View.VISIBLE
                }
            }

            init()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_men2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_html -> { /* Handle edit */ true }
            R.id.fullscreen -> { /* Handle fullscreen */ true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun changeBgColor(isAvailable: Boolean) {
        runOnUiThread {
            val navHeaderBinding = NavHeaderBinding.bind(binding.myNav.getHeaderView(0))
            val colorRes = if (isAvailable) R.color.dark_teal_200 else R.color.black
            navHeaderBinding.background.setBackgroundColor(ContextCompat.getColor(this, colorRes))
        }
    }

    private fun initOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val drawer = binding.drawerLayout

                when {
                    drawer.isDrawerOpen(binding.myNav) -> drawer.closeDrawer(binding.myNav)
                    drawer.isDrawerOpen(binding.nav2) -> drawer.closeDrawer(binding.nav2)
                    mainWebView.canGoBack() -> mainWebView.goBack()
                    else -> {
                        val currentTime = System.currentTimeMillis()
                        // Check if the current time is within the 2-second window of last press
                        if (currentTime - lastBackPressTime < BACK_PRESS_INTERVAL) {
                            finish()
                        } else {
                            lastBackPressTime = currentTime
                            alert("Press again to exit")
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra(Consts.intentLoadUrl, false)) {
            intent.getStringExtra(Consts.intentUrl)?.let { url ->
                mainWebView.loadUrl(url)
                urlBarComponent?.setText(url)
            }
        }
    }

    override fun onDestroy() {
        urlBarComponent?.release()
        mainWebView.destroyWebView()
        networkCallback?.let {
            NetworkHelper.unregisterNetworkCallback(this, it)
            networkCallback = null
        }
        super.onDestroy()
    }

    companion object {
        var lastBackPressTime: Long = 0L
        private const val BACK_PRESS_INTERVAL = 2000L // 2 seconds
    }
}
