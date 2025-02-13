package com.sk.revisit.webview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.DownloadListener;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sk.revisit.log.Log;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyDownloadListener3 implements DownloadListener {

	private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
	private final String TAG = this.getClass().getSimpleName();
	private final Context context;
	private final OkHttpClient client = new OkHttpClient();
	private final MySettingsManager settingsManager;
	private final PermissionHandler permissionHandler;
	private final UserInterfaceHandler uiHandler;
	private final ActivityResultLauncher<Intent> folderPickerLauncher;

	public MyDownloadListener3(Context context, ActivityResultLauncher<Intent> folderPickerLauncher) {
		this.context = context;
		this.settingsManager = new MySettingsManager(context);
		this.permissionHandler = new PermissionHandler(context);
		this.uiHandler = new UserInterfaceHandler(context);
		this.folderPickerLauncher = folderPickerLauncher;
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		if (!permissionHandler.hasStoragePermission()) {
			uiHandler.showToast("Storage permission is required to download files");
			permissionHandler.requestStoragePermission((Activity) context);
			return;
		}

		String filename = FileUtils.getFilenameFromContentDisposition(contentDisposition, url);
		downloadFile(url, filename, mimetype);
	}

	private void downloadFile(String url, String filename, String mimetype) {
		String downloadDir = settingsManager.getDownloadStoragePath();

		if (downloadDir == null) {
			uiHandler.askUserForDownloadLocation(settingsManager);
			return;
		}

		if (!FileUtils.createDirectory(new File(downloadDir))) {
			uiHandler.showToast("Failed to create directory");
			uiHandler.askUserForDownloadLocation(settingsManager);
			return;
		}

		Request request = new Request.Builder().url(url).build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				Log.e(TAG, "Download failed: " + e.getMessage());
				uiHandler.showToast("Download failed: " + e.getMessage());
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
				if (!response.isSuccessful()) {
					Log.e(TAG, "Unexpected code " + response);
					uiHandler.showToast("Download failed: Unexpected code " + response.code());
					return;
				}

				try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream()) {
					File file = new File(downloadDir, filename);
					FileUtils.saveFile(inputStream, file);
					uiHandler.showToast("Download complete: " + filename);

				} catch (IOException e) {
					Log.e(TAG, "Error writing file: " + e.getMessage());
					uiHandler.showToast("Error writing file: " + e.getMessage());
				}
			}
		});
	}

	// --- Helper Classes ---

	public void handleDirectorySelection(Uri uri) {
		if (uri != null) {
			File directory = new File(Objects.requireNonNull(uri.getPath()));
			settingsManager.setDownloadStoragePath(directory.getAbsolutePath());
			uiHandler.showToast("Download location set successfully.");
		} else {
			uiHandler.showToast("Directory selection cancelled.");
		}
	}

	private static class FileUtils {
		static String getFilenameFromContentDisposition(String contentDisposition, String url) {
			String filename = null;
			if (contentDisposition != null) {
				try {
					String[] parts = contentDisposition.split("filename=");
					if (parts.length > 1) {
						filename = parts[1].replaceAll("\"", "").trim();
					}
				} catch (Exception e) {
					Log.e("FileUtils", e.toString());
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

		static boolean createDirectory(File directory) {
			return directory.exists() || directory.mkdirs();
		}

		static void saveFile(InputStream inputStream, File file) throws IOException {
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();
			}
		}
	}

	private class PermissionHandler {
		private final Context context;

		PermissionHandler(Context context) {
			this.context = context;
		}

		boolean hasStoragePermission() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				return Environment.isExternalStorageManager();
			} else {
				return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
			}

		}

		void requestStoragePermission(Activity activity) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				try {
					Intent intent = new Intent();
					intent.setAction("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION");
					Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
					intent.setData(uri);
					activity.startActivity(intent);
				} catch (Exception e) {
					Intent intent = new Intent();
					intent.setAction("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION");
					activity.startActivity(intent);
				}
			} else {
				if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					new AlertDialog.Builder(context)
							.setTitle("Storage Permission Needed")
							.setMessage("This app needs the storage permission to download files.")
							.setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE))
							.setNegativeButton("Cancel", (dialog, which) -> {
								dialog.dismiss();
								uiHandler.showToast("Download cancelled");
							})
							.create()
							.show();
				} else {
					ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
				}
			}
		}
	}

	private class UserInterfaceHandler {
		private final Context context;

		UserInterfaceHandler(Context context) {
			this.context = context;
		}

		void showToast(String message) {
			new android.os.Handler(context.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
		}

		void askUserForDownloadLocation(MySettingsManager settingsManager) {
			new android.os.Handler(context.getMainLooper()).post(() -> new AlertDialog.Builder(context)
					.setTitle("Download Location Required")
					.setMessage("Please select a download location.")
					.setPositiveButton("OK", (dialog, which) -> {
						pickFolder();
					})
					.setNegativeButton("Cancel", (dialog, which) -> {
						dialog.dismiss();
						showToast("Download cancelled");
					})
					.show());
		}

		void pickFolder() {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			folderPickerLauncher.launch(intent);
		}
	}
}