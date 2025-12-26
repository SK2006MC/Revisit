package com.sk.revisit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sk.revisit.data.Url
import com.sk.revisit.databinding.ItemUrlBinding

class UrlAdapter(private val urlList: List<Url>) : RecyclerView.Adapter<UrlAdapter.UrlViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val urlBinding = ItemUrlBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UrlViewHolder(urlBinding)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        holder.bind(urlList[position])
    }

    override fun getItemCount(): Int = urlList.size

    class UrlViewHolder(private val binding: ItemUrlBinding) :
        RecyclerView.ViewHolder(binding.root), Url.OnProgressChangeListener {

        private var currentUrl: Url? = null

        init {
            binding.urlCheckbox.setOnCheckedChangeListener { _, isChecked ->
                currentUrl?.isSelected = isChecked
            }
        }

        fun bind(url: Url) {
            currentUrl = url
            binding.urlTextview.text = url.url
            binding.sizeTextview.text = if (url.size > 0) "${url.size} bytes" else "Calculating..."
            binding.urlCheckbox.isChecked = url.isSelected

            // Set the listener to this ViewHolder instance
            url.setOnProgressChangeListener(this)
        }

        override fun onChange(p: Double) {
            binding.progressBar.progress = p.toInt()
        }
    }

    companion object {
        val TAG: String = UrlAdapter::class.java.simpleName
    }
}