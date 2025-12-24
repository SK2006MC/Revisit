package com.sk.revisit.activities

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sk.revisit.GVars
import com.sk.revisit.MyUtils
import com.sk.revisit.R
import com.sk.revisit.Revisit
import com.sk.revisit.adapter.UrlAdapter
import com.sk.revisit.data.Url
import com.sk.revisit.databinding.ActivityDownloadBinding
import com.sk.revisit.managers.MySettingsManager
import okhttp3.Headers
import okhttp3.Request
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class DownloadActivity : BaseActivity() {

    companion object {
        private const val FORMAT = "Total Size: %d bytes"
    }

    private val urlsStr = mutableSetOf<String>()
    private val urlsList = mutableListOf<Url>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var binding: ActivityDownloadBinding
    private lateinit var myUtils: MyUtils
    private lateinit var settingsManager: MySettingsManager
    private lateinit var urlsAdapter: UrlAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsManager = MySettingsManager(this)
        myUtils = MyUtils(this, settingsManager.rootStoragePath)

        urlsAdapter = UrlAdapter(urlsList)
        initUI()
        loadUrlsFromFile()
    }

    private fun loadUrlsFromFile() {
        urlsStr.clear()
        urlsList.clear()
        urlsAdapter.notifyDataSetChanged()

        val filePath = settingsManager.rootStoragePath + File.separator + GVars.reqFileName
        val reqFile = File(filePath)

        if (!reqFile.exists()) {
            Log.e(TAG, "${GVars.reqFileName} not found at: $filePath")
            alert("${GVars.reqFileName} not found at: $filePath")
            return
        }

        try {
            BufferedReader(FileReader(reqFile)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    val urlStr = line.trim()
                    urlsStr.add(urlStr)
                    val url = Url(urlStr)
                    urlsList.add(url)
                    urlsAdapter.notifyItemInserted(urlsList.size - 1)
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            alert("Error reading $filePath")
            Log.e(TAG, "Error reading $filePath", e)
        }

        saveToFile(urlsStr, reqFile)
    }

    private fun initUI() {
        binding.totalSizeTextview.text = getString(R.string.total)
        binding.refreshButton.setOnClickListener { loadUrlsFromFile() }

        binding.calcButton.setOnClickListener { calculateTotalSize() }
        binding.downloadButton.setOnClickListener { downloadSelectedUrls() }

        binding.urlsRecyclerview.apply {
            adapter = urlsAdapter
            layoutManager = LinearLayoutManager(this@DownloadActivity)
            val decoration = DividerItemDecoration(this@DownloadActivity, LinearLayoutManager.VERTICAL)
            decoration.setDrawable(
                ContextCompat.getDrawable(this@DownloadActivity, R.drawable.divider)
                    ?: return@apply
            )
            addItemDecoration(decoration)
        }
    }

    private fun downloadSelectedUrls() {
        val selectedPositions = urlsList.mapIndexedNotNull { index, url -> if (url.isSelected) index else null }

        if (selectedPositions.isEmpty()) {
            alert("No URLs selected for download.")
            return
        }

        for (position in selectedPositions) {
            val url = urlsList[position]
            myUtils.download(Uri.parse(url.url), object : MyUtils.DownloadListener {
                private fun notifyUpdate() {
                    mainHandler.post { urlsAdapter.notifyItemChanged(position) }
                }

                override fun onStart(uri: Uri, contentLength: Long) {
                    url.size = contentLength
                }

                override fun onSuccess(file: File, headers: Headers) {
                    url.isDownloaded = true
                    url.setProgress(100.0)
                    notifyUpdate()
                }

                override fun onProgress(p: Double) {
                    url.setProgress(p)
                    notifyUpdate()
                }

                override fun onFailure(e: Exception) {
                    url.isDownloaded = false
                    notifyUpdate()
                }

                override fun onEnd(file: File) {}
            })
        }
    }

    private fun calculateTotalSize() {
        if (!Revisit.isNetworkAvailable) {
            alert("No network available!")
            return
        }

        myUtils.executorService.execute {
            val totalSize = AtomicLong(0)
            for (url in urlsList) {
                val request = Request.Builder().head().url(url.url).build()
                try {
                    val response = myUtils.client.newCall(request).execute()
                    response.use {
                        if (it.isSuccessful) {
                            url.size = it.body?.contentLength() ?: -1
                            totalSize.addAndGet(url.size)
                        } else {
                            url.size = -1
                        }
                    }
                    mainHandler.post { urlsAdapter.notifyItemChanged(urlsList.indexOf(url)) }
                } catch (e: IOException) {
                    Log.e(TAG, " ", e)
                }
            }
            mainHandler.post {
                binding.totalSizeTextview.text = String.format(Locale.ENGLISH, FORMAT, totalSize.get())
            }
        }
    }

    private fun saveToFile(urls: Set<String>, file: File) {
        myUtils.executorService.execute {
            try {
                BufferedWriter(FileWriter(file)).use { writer ->
                    for (url in urls) {
                        writer.write(url)
                        writer.newLine()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }
        }
    }
}
