package com.sk.revisit.managers;

import android.net.Uri;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit.MyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.Headers;

public class WebStorageManager {
	private static final String TAG = "WebStorageManager";
	private static final String GET_METHOD = "GET";
	private static final String UTF_8 = "UTF-8";
	private static final String NO_OFFLINE_FILE_MESSAGE = "No offline file available.";
	private final MyUtils utils;

	public WebStorageManager(MyUtils utils) {
		this.utils = utils;
	}

	@Nullable
	public WebResourceResponse getResponse(@NonNull WebResourceRequest request) {
		MyUtils.requests.incrementAndGet();

		if (!GET_METHOD.equals(request.getMethod())) {
			utils.log(TAG, "Request method is not GET: " + request.getMethod());
			return null;
		}

		Uri uri = request.getUrl();
		String uriStr = uri.toString();
		utils.saveUrl(uriStr);

		if (!URLUtil.isNetworkUrl(uriStr)) {
			utils.log(TAG, "Not a network URL: " + uriStr);
			return null;
		}

		String localPath = utils.buildLocalPath(uri);
		if (localPath == null) {
			utils.log(TAG, "Could not build local path for: " + uriStr);
			return null;
		}

		File localFile = new File(localPath);
		if (localFile.exists()) {
			if (MyUtils.shouldUpdate && MyUtils.isNetworkAvailable) {
				utils.download(uri, createDownloadListener(uriStr, localPath));
			}
			return loadFromLocal(localFile, uri);
		} else {
			if (MyUtils.isNetworkAvailable) {
				utils.download(uri, createDownloadListener(uriStr, localPath));
				return loadFromLocal(localFile, uri);
			}
			utils.saveReq(uriStr);
			return createNoOfflineFileResponse();
		}
	}

	@NonNull
	private MyUtils.DownloadListener createDownloadListener(String uriStr, String localPath) {
		return new MyUtils.DownloadListener() {
			@Override
			public void onSuccess(File file, Headers headers) {
				MyUtils.resolved.incrementAndGet();
				utils.saveResp(String.format("[\"%s\",\"%s\",%d,\"%s\"]", uriStr, localPath, file.length(), headers.toString()));
			}

			@Override
			public void onFailure(Exception e) {
				MyUtils.failed.incrementAndGet();
				utils.saveReq(uriStr);
				utils.log(TAG, "Download failed for: " + uriStr, e);
			}
		};
	}

	@Nullable
	private WebResourceResponse loadFromLocal(@NonNull File localFile, Uri uri) {
		String localFilePath = localFile.getAbsolutePath();
		try {
			String mimeType = getMimeType(localFilePath, uri);
			MyUtils.resolved.incrementAndGet();
			InputStream inputStream = new FileInputStream(localFile);
			return new WebResourceResponse(mimeType, UTF_8, inputStream);
		} catch (FileNotFoundException e) {
			MyUtils.failed.incrementAndGet();
			utils.log(TAG, "File not found: " + localFile.getAbsolutePath(), e);
			return null;
		}
	}

	@NonNull
	private String getMimeType(String localFilePath, Uri uri) {
		String mimeType = utils.getMimeTypeFromMeta(localFilePath);
		if (mimeType == null) {
			utils.createMimeTypeMeta(uri);
			mimeType = utils.getMimeType(localFilePath);
		}
		return mimeType;
	}

	@NonNull
	private WebResourceResponse createNoOfflineFileResponse() {
		return new WebResourceResponse("text/html", UTF_8, new ByteArrayInputStream(NO_OFFLINE_FILE_MESSAGE.getBytes()));
	}
}