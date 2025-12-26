package com.sk.revisit.data

class UrlLog(val url: String, val size: Long) {
    var p: Double = 0.0
    var isComplete: Boolean = false
    private var listener: OnProgressChangeListener? = null

    fun setProgress(p: Double) {
        this.p = p
        listener?.onChange(p)
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener?) {
        this.listener = listener
    }

    fun interface OnProgressChangeListener {
        fun onChange(p: Double)
    }
}
