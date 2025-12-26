package com.sk.revisit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sk.revisit.data.ItemPage
import com.sk.revisit.databinding.ItemPageBinding

class WebpageItemAdapter(
    private var webPagesOrg: List<ItemPage> = emptyList(),
    private val onItemClick: (ItemPage) -> Unit // Added for easy click handling
) : RecyclerView.Adapter<WebpageItemAdapter.ViewHolder>() {

    // The list currently being displayed (filtered)
    private var webPages: List<ItemPage> = webPagesOrg

    fun updateData(newList: List<ItemPage>) {
        val diffCallback = WebpageDiffCallback(webPages, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.webPagesOrg = newList
        this.webPages = newList

        diffResult.dispatchUpdatesTo(this)
    }

    fun filter(query: String) {
        val filteredList = if (query.isBlank()) {
            webPagesOrg
        } else {
            webPagesOrg.filter { it.fileName.contains(query, ignoreCase = true) }
        }

        val diffCallback = WebpageDiffCallback(webPages, filteredList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.webPages = filteredList
        diffResult.dispatchUpdatesTo(this)
    }

    fun sortBySize() {
        val sortedList = webPages.sortedByDescending { it.size }
        updateDisplayedList(sortedList)
    }

    private fun updateDisplayedList(newList: List<ItemPage>) {
        val diffCallback = WebpageDiffCallback(webPages, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.webPages = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(webPages[position])
    }

    override fun getItemCount(): Int = webPages.size

    inner class ViewHolder(private val binding: ItemPageBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(webPages[position])
                }
            }
        }

        fun bind(page: ItemPage) = with(binding) {
            host.text = page.host
            size.text = page.sizeStr
            nametext.text = page.fileName
        }
    }

    private class WebpageDiffCallback(
        private val oldList: List<ItemPage>,
        private val newList: List<ItemPage>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            // Compare unique identifiers (fileName in this case)
            return oldList[oldPos].fileName == newList[newPos].fileName
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}