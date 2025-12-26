package com.sk.revisit.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log

/**
 * NetworkHelper
 *
 * Small utility to register/unregister a ConnectivityManager.NetworkCallback and forward availability changes
 * to a listener. Keeps MainActivity cleaner.
 */
object NetworkHelper {

    private const val TAG = "NetworkHelper"

    fun interface NetworkListener {
        fun onNetworkChanged(isAvailable: Boolean)
    }

    /**
     * Register network callback and return the created callback so caller can later unregister it.
     *
     * @param context  required to access ConnectivityManager
     * @param listener will receive true when network with INTERNET capability is available, false when lost
     * @return the registered ConnectivityManager.NetworkCallback
     */
    fun registerNetworkCallback(context: Context, listener: NetworkListener): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "onAvailable network")
                listener.onNetworkChanged(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "onLost network")
                listener.onNetworkChanged(false)
            }
        }

        connectivityManager?.registerNetworkCallback(request, callback)
        return callback
    }

    /**
     * Unregister previously created network callback.
     *
     * @param context  same Context used to register
     * @param callback the callback returned by registerNetworkCallback
     */
    fun unregisterNetworkCallback(context: Context, callback: ConnectivityManager.NetworkCallback) {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            connectivityManager?.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            // ignore issues during unregister
            e.printStackTrace()
        }
    }
}
