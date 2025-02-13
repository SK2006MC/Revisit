package com.sk.revisit.helper;

import androidx.annotation.NonNull;

import com.sk.revisit.log.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetHelper {
	String TAG = NetHelper.class.getSimpleName();
	OkHttpClient okHttpClient;

	public NetHelper(OkHttpClient client){
		this.okHttpClient = client;
	}

	public Response head(String url){
		Request request = new Request.Builder().head().url(url).build();
		long size;
		try {
			okHttpClient.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e) {

				}

				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
					assert response.body() != null;
//					size = response.body().contentLength();
				}
			});
		} catch (Exception e) {
			Log.e(TAG,e.toString());
		}
		return null;
	}
}
