package com.sk.revisit.helper

import android.util.Log
import okhttp3.*
import java.io.IOException

class NetHelper(private val okHttpClient: OkHttpClient) {
    private val TAG = NetHelper::class.java.simpleName

    fun head(url: String): Response? {
        val request = Request.Builder().head().url(url).build()
        try {
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Head request failed", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let {
                        // size = it.contentLength()
                    }
                    response.close()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        return null
    }
}
