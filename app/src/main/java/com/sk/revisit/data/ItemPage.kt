package com.sk.revisit.data

class ItemPage {
    @JvmField
    var fileName: String? = null
    @JvmField
    var host: String? = null
    @JvmField
    var size: Long = 0
    @JvmField
    var sizeStr: String? = null

    fun getSize(): Long = size
}
