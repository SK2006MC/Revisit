package com.sk.revisit.components

import android.webkit.WebView
import com.sk.revisit.activities.BaseActivity
import com.sk.revisit.databinding.NavJsBinding
import com.sk.revisit.jsconsole.JSConsoleLogger
import com.sk.revisit.jsconsole.JSWebViewManager

class JSNavComponent(
    activity: BaseActivity,
    private val binding: NavJsBinding,
    webView: WebView
) : Component(activity) {

    val jsConsoleLogger = JSConsoleLogger(context, binding.consoleLayout, binding.consoleScrollView)
    private val jsWebViewManager = JSWebViewManager(context, webView, jsConsoleLogger)

    init {
        setupListeners(webView)
    }

    private fun setupListeners(webView: WebView) {
        binding.executeJsBtn.apply {
            setOnClickListener {
                val code = binding.jsInput.text.toString()
                jsWebViewManager.executeJS(code) { result ->
                    jsConsoleLogger.logConsoleMessage(">$code\n$result\n")
                }
            }

            setOnLongClickListener {
                binding.consoleLayout.removeAllViewsInLayout()
                true
            }

            tooltipText = "Click to execute.\nLong click to clear logs."
        }

        binding.jsInput.setWebView(webView)
    }
}