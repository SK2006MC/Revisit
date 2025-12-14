package com.sk.revisit.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * NetworkHelper
 *
 * Small utility to register/unregister a ConnectivityManager.NetworkCallback and forward availability changes
 * to a listener. Keeps MainActivity cleaner.
 */
public final class NetworkHelper {

    private static final String TAG = "NetworkHelper";

    private NetworkHelper() { /* no-op */ }

    public interface NetworkListener {
        void onNetworkChanged(boolean isAvailable);
    }

    /**
     * Register network callback and return the created callback so caller can later unregister it.
     *
     * @param context  required to access ConnectivityManager
     * @param listener will receive true when network with INTERNET capability is available, false when lost
     * @return the registered ConnectivityManager.NetworkCallback
     */
    public static ConnectivityManager.NetworkCallback registerNetworkCallback(@NonNull Context context, @NonNull NetworkListener listener) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d(TAG, "onAvailable network");
                listener.onNetworkChanged(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d(TAG, "onLost network");
                listener.onNetworkChanged(false);
            }
        };

        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(request, callback);
        }
        return callback;
    }

    /**
     * Unregister previously created network callback.
     *
     * @param context  same Context used to register
     * @param callback the callback returned by registerNetworkCallback
     */
    public static void unregisterNetworkCallback(@NonNull Context context, @NonNull ConnectivityManager.NetworkCallback callback) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(callback);
            }
        } catch (Exception e) {
            // ignore issues during unregister
            e.printStackTrace();
        }
    }
}