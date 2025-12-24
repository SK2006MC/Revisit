package com.sk.revisit.data

class Url(val url: String) {
    val isUpdateAvailable: Boolean = false
    var size: Long = 0
    var isDownloaded: Boolean = false
    var isSelected: Boolean = false
    var listener: OnProgressChangeListener? = null

    var progress: Double = 0.0
    set(p) {
        field = p
        listener?.onChange(p)
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener?) {
        if (listener == null) return
        if (this.listener == null) {
            this.listener = listener
        }
    }

    fun interface OnProgressChangeListener {
        fun onChange(p: Double)
    }
}
