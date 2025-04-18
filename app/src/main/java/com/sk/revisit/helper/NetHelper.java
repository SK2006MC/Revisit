package com.sk.revisit.helper;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetHelper {
    final String TAG = NetHelper.class.getSimpleName();
    final OkHttpClient okHttpClient;

    public NetHelper(OkHttpClient client) {
        this.okHttpClient = client;
    }

    public Response head(String url) {
        Request request = new Request.Builder().head().url(url).build();
        long size;
        try {
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    assert response.body() != null;
                    // size = response.body().contentLength();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
}
