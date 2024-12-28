package com.sk.revisit.managers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.sk.revisit.MyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class WebStorageManager {

    MySettingsManager settingsManager;

    MyUtils utils;

    String TAG = "WebStorageManager";

    public WebStorageManager(Context context) {
        this.settingsManager = new MySettingsManager(context);
        this.utils = new MyUtils(this.settingsManager.getRootStoragePath());
    }

    public WebResourceResponse getStoredResponse(WebResourceRequest request) {
        if (request.getMethod().equals("GET")) {

            Uri uri = request.getUrl();
            String localPath = utils.buildLocalPath(uri);
            File file = new File(localPath);

            if (file.exists()) {

            }
        }
        return null;
    }

    WebResourceResponse loadFromLocal(Uri uri) {
        String filePath = utils.buildLocalPath(uri);
        FileInputStream is;
        WebResourceResponse response;
        try {
            is = new FileInputStream(filePath);
            response = new WebResourceResponse("text", "utf-8", is);
        } catch (Exception e) {
            response = new WebResourceResponse("text", "utf-8", new ByteArrayInputStream("err".getBytes(StandardCharsets.UTF_8)));
            Log.d(TAG, e.toString());
        }
        return response;
    }
}