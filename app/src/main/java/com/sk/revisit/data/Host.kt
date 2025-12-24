package com.sk.revisit.data

import java.util.ArrayList

data class Host(
    var name: String,
    var isSelected: Boolean = false,
    var isExpanded: Boolean = false,
    var urls: MutableList<Url> = mutableListOf(),
    var totalSize: Long = 0
) {
    fun addUrl(url: Url) {
        this.urls.add(url)
    }
}
