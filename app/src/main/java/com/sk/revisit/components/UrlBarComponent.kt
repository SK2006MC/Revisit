package com.sk.revisit.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.sk.revisit.activities.BaseActivity
import com.sk.revisit.helper.FileHelper
import com.sk.revisit.webview.MyWebView
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class UrlBarComponent(
    activity: BaseActivity,
    private val urlAutoCompleteTextView: AppCompatAutoCompleteTextView,
    private val mainWebView: MyWebView,
    private val rootPath: String
) : Component(activity) {

    // Coroutine scope for background file searching
    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        setupUrlBarListeners()
        setupAutoComplete(File(rootPath))
    }

    private fun setupUrlBarListeners() {
        urlAutoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) loadUrlFromInput()
        }

        urlAutoCompleteTextView.setOnEditorActionListener { _, _, _ ->
            loadUrlFromInput()
            true
        }
    }

    private fun loadUrlFromInput() {
        val url = urlAutoCompleteTextView.text.toString().trim()
        if (url.isNotEmpty()) {
            try {
                mainWebView.loadUrl(url)
            } catch (e: Exception) {
                alert("Error loading URL: ${e.message}")
            }
        }
    }

    fun setupAutoComplete(directory: File) {
        componentScope.launch {
            val fileNames = withContext(Dispatchers.IO) {
                try {
                    FileHelper.search(directory, ".html")
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) { alert("Error searching files: ${e.message}") }
                    emptyList<String>()
                }
            }

            if (fileNames.isNotEmpty()) {
                urlAutoCompleteTextView.setAdapter(CustomCompletionAdapter(activity, fileNames))
            }
        }
    }

    fun setText(text: String) {
        activity.runOnUiThread { urlAutoCompleteTextView.setText(text) }
    }

    fun release() {
        componentScope.cancel() // Cancels any ongoing background searches
    }
}

class CustomCompletionAdapter(context: Context, data: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, data) {

    private val originalList = data.toList()
    private var filteredList = data.toList()

    override fun getCount() = filteredList.size
    override fun getItem(position: Int) = filteredList.getOrNull(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)

        view.findViewById<TextView>(android.R.id.text1).text = getItem(position)
        return view
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val filtered = if (constraint.isNullOrEmpty()) {
                originalList
            } else {
                val query = constraint.toString().lowercase()
                originalList.filter { it.lowercase().contains(query) }
            }
            results.values = filtered
            results.count = filtered.size
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredList = results?.values as? List<String> ?: originalList
            notifyDataSetChanged()
        }
    }
}