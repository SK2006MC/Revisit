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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
			//utils.log(TAG, "Request method is not GET: " + request.getMethod());
			return null;
		}

		Uri uri = request.getUrl();
		String uriStr = uri.toString();
		utils.saveUrl(uriStr);

		if (!URLUtil.isNetworkUrl(uriStr)) {
			//utils.log(TAG, "Not a network URL: " + uriStr);
			return null;
		}

		String localPath = utils.buildLocalPath(uri);
		if (localPath == null) {
			utils.log(TAG, "Could not build local path for: " + uriStr);
			return null;
		}

		File localFile = new File(localPath);
		boolean fileExists = localFile.exists();
		//utils.log(TAG,localPath+",exists="+fileExists);

		if (fileExists) {
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
			//urlLog
			@Override
			public void onStart(Uri uri,long contentLength){
				//ToDo create a item_url_log
				//urlLog.text = uri.toString()
			}
			
			@Override
			public void onSuccess(File file, Headers headers) {
				MyUtils.resolved.incrementAndGet();
//				utils.saveResp(String.format(Locale.ENGLISH,"[\"%s\",\"%s\",%d,\"%s\"]", uriStr, localPath, file.length(), headers.toString()));
			}

			@Override
			public void onProgress(double p) {
				//urlLog.pb.setProgress(p)
			}

			@Override
			public void onFailure(Exception e) {
				MyUtils.failed.incrementAndGet();
				utils.saveReq(uriStr);
//				utils.log(TAG, "Download failed for: " + uriStr, e);
			}
			
			@Override
			public void onEnd(File file){
				//urllog.pb.visible=gone
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
			WebResourceResponse response = new WebResourceResponse(mimeType, UTF_8, inputStream);
			response.setResponseHeaders(Collections.singletonMap("Access-Control-Allow-Origin","*"));
			return response;
		} catch (FileNotFoundException e) {
			MyUtils.failed.incrementAndGet();
			//utils.log(TAG, "File not found: " + localFile.getAbsolutePath(), e);
			return null;
		}
	}

	@NonNull
	private String getMimeType(String localFilePath, Uri uri) {
		String mimeType = utils.getMimeTypeFromMeta(localFilePath);
		if (mimeType == null) {
			utils.createMimeTypeMeta(uri);
			if(localFilePath.contains(":")){
					localFilePath = localFilePath.split(":")[0];
			}
			mimeType = utils.getMimeType(localFilePath);
		}
		return mimeType;
	}

	Map<String,String> getHeaders(String path,Uri uri){
		Map<String,String> headers = new HashMap<>();
		path = path+".head";
		File file = new File(path);
//		if(file.exists()){
//				headersb = parse(file);
//		}else{
//			//makes a head req for the uri saves it to the path
//			createHeadersFile(uri,path);
//		}
		return null;
	}
	
	@NonNull
	private WebResourceResponse createNoOfflineFileResponse() {
		return new WebResourceResponse("text/html", UTF_8, new ByteArrayInputStream(NO_OFFLINE_FILE_MESSAGE.getBytes()));
	}
}