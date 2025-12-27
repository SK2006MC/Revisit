package com.sk.revisit.activities

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.sk.revisit.Consts
import com.sk.revisit.data.ItemPage
import com.sk.revisit.databinding.ActivityWebpagesBinding
import com.sk.revisit.managers.MySettingsManager
import com.sk.revisit.managers.WebpageRepository
import com.sk.revisit.adapter.WebpageItemAdapter

class WebpagesActivity : BaseActivity(), WebpageRepository.Callback {

    private lateinit var binding: ActivityWebpagesBinding
    private lateinit var pageItemAdapter: WebpageItemAdapter
    private lateinit var webpageRepository: WebpageRepository

    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebpagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val settingsManager = MySettingsManager(this)
        val rootPath = settingsManager.rootStoragePath

        // Initialize Repository
        webpageRepository = WebpageRepository(this, rootPath)

        initUi()
        loadWebpages()
    }

    private fun initUi() = with(binding) {
        webpagesHosts.layoutManager = LinearLayoutManager(this@WebpagesActivity)

        // Initialize Adapter with click listener
        pageItemAdapter = WebpageItemAdapter { page ->
            navigateToMain(page.fileName)
        }
        webpagesHosts.adapter = pageItemAdapter

        webpagesRefreshButton.setOnClickListener { loadWebpages() }

        // Debounced search logic using KTX doAfterTextChanged
        searchBar.doAfterTextChanged { text ->
            searchRunnable?.let { searchBar.removeCallbacks(it) }
            searchRunnable = Runnable { filterPagesByKeywords(text?.toString().orEmpty()) }
            searchBar.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        }
    }

    private fun loadWebpages() = with(binding) {
        progressBar.visibility = View.VISIBLE
        webpagesHosts.visibility = View.GONE
        pageItemAdapter.updateData(emptyList()) // Clear old data

        try {
            webpageRepository.loadWebpages(this@WebpagesActivity)
        } catch (e: Exception) {
            onError(e.toString())
            progressBar.visibility = View.GONE
        }
    }

    // --- WebpageRepository.Callback Implementation ---

    override fun onSuccess(pages: List<ItemPage>) {
        // Repository already returns on Main Thread via Coroutines
        pageItemAdapter.updateData(pages)

        if (pages.isEmpty()) {
            alert("No HTML files found in the root directory.")
        }

        binding.progressBar.visibility = View.GONE
        binding.webpagesHosts.visibility = View.VISIBLE
    }

    override fun onError(message: String) {
        alert(message)
        binding.progressBar.visibility = View.GONE
        binding.webpagesHosts.visibility = View.VISIBLE
    }

    // --- Helper Methods ---

    private fun filterPagesByKeywords(keywords: String) {
        pageItemAdapter.filter(keywords)
    }

    private fun navigateToMain(filename: String) {
        startMyActivity<MainActivity> {
            putExtra(Consts.intentLoadUrl, true)
            putExtra(Consts.intentUrl, filename)
        }
        alert("Loading... $filename")

        // Finish previous activity and this one
        revisitApp.lastActivity?.finish()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        webpageRepository.shutdown()
        searchRunnable?.let { binding.searchBar.removeCallbacks(it) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 300L
    }
}
