package com.sk.revisit.webview;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.webkit.DownloadListener;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.sk.revisit.log.Log;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//for a webview
public class MyDownloadListener implements DownloadListener {
	private final String TAG = this.getClass().getSimpleName();
	private final Context context;
	private final OkHttpClient client = new OkHttpClient(); // Use OkHttp client
	MySettingsManager sm;

	public MyDownloadListener(Context context) {
		this.context = context;
		this.sm = new MySettingsManager(context);
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition,
								String mimetype, long contentLength) {
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(context, "Storage permission is required to download files", Toast.LENGTH_SHORT).show();
			// TODO call a function to request permissions here
			return;
		}

		String filename = getFilenameFromContentDisposition(contentDisposition, url);
		downloadFile(url, filename, mimetype);
	}

	private void downloadFile(String url, String filename, String mimetype) {
		Request request = new Request.Builder().url(url).build();

		new Thread(() -> {  // Run on background thread
			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				}

				assert response.body() != null;
				InputStream inputStream = response.body().byteStream();

				File downloadDir = new File(sm.getDownloadStoragePath());
				if (!downloadDir.exists()) {
					if (!downloadDir.mkdirs()) {
						showToast("Failed to create directory");
						//TODO ask user for location then save in setings use sm.setDownloadStoragePath(path);
						return;
					}
				}

				File file = new File(downloadDir, filename);

				try (FileOutputStream outputStream = new FileOutputStream(file)) {
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
					outputStream.flush();
					showToast("Download complete: " + filename); // Download complete
				} catch (IOException e) {
					Log.e(TAG, e.toString());
					showToast("Error writing file: " + e.getMessage());
				} finally {
					inputStream.close(); // Close input stream
				}
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				showToast("Download failed: " + e.getMessage()); // Network or server error
			}
		}).start();
	}

	private String getFilenameFromContentDisposition(String contentDisposition, String url) {
		String filename = null;
		if (contentDisposition != null) {
			try {
				String[] parts = contentDisposition.split("filename=");
				if (parts.length > 1) {
					filename = parts[1].replaceAll("\"", "").trim();
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
		if (filename == null || filename.isEmpty()) {
			filename = url.substring(url.lastIndexOf('/') + 1);
			if (filename.isEmpty()) {
				filename = "downloaded_file";
			}
		}
		return filename;
	}

	private void showToast(String message) {
		// Ensure the toast runs on the UI thread
		new android.os.Handler(context.getMainLooper()).post(() ->
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
		);
	}
}