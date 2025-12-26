package com.sk.revisit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sk.revisit.data.UrlLog
import com.sk.revisit.databinding.ItemUrllogBinding

class UrlsLogAdapter(
    private var urlLogs: List<UrlLog> = emptyList()
) : RecyclerView.Adapter<UrlsLogAdapter.UrlViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val binding = ItemUrllogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UrlViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        holder.bind(urlLogs[position])
    }

    override fun getItemCount(): Int = urlLogs.size

    // Added a helper method to update the data
    fun submitList(newList: List<UrlLog>) {
        urlLogs = newList
        notifyDataSetChanged()
    }

    class UrlViewHolder(private val binding: ItemUrllogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(urlLog: UrlLog) {
            binding.urlText.text = urlLog.url

            // Note: In Android, .setText(int) looks for a String Resource ID.
            // If urlLog.size is a raw number, use .toString() to avoid a Crash.
            binding.size.text = urlLog.size.toInt().toString()

            urlLog.setOnProgressChangeListener { progress ->
                binding.progress.progress = progress.toInt()
            }
        }
    }
}