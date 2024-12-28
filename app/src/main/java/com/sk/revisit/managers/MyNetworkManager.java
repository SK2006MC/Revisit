package com.sk.revisit.managers;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MyNetworkManager {
    String TAG = "MyNetworkManager";

    public void downloadUrlToFile(Uri url, File file) {
        try {

        } catch (Exception e) {

        }
    }

    public void downloadUrl(String urlString, File file) {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            //responseData.statusCode = connection.getResponseCode();
            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                //responseData.inputStream = inputStream;
                Map<String, String> headers = getConnectionHeaders(connection);
            } else {
                //responseData.errorMessage = "HTTP error code: " + responseData.statusCode;
            }

        } catch (IOException e) {
            //responseData.errorMessage = "Download error: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        //return responseData;
    }

    public Map<String, String> getHeadRequestHeaders(String urlString) {
        HttpURLConnection connection = null;
        Map<String, String> headers = new HashMap<>();
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                headers = getConnectionHeaders(connection);
            }

        } catch (IOException e) {
            Log.d(TAG, e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return headers;
    }

    private Map<String, String> getConnectionHeaders(HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>();
        Map<String, java.util.List<String>> headerFields = connection.getHeaderFields();
        if (headerFields != null) {
            for (Map.Entry<String, java.util.List<String>> entry : headerFields.entrySet()) {
                String key = entry.getKey();
                if (key != null) {
                    headers.put(key, entry.getValue().get(0)); // Assuming single value per header for simplicity
                }
            }
        }
        return headers;
    }


}
