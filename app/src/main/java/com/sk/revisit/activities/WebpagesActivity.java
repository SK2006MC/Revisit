package com.sk.revisit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.log.Log;
import com.sk.revisit.adapter.WebpageItemAdapter;
import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebpagesActivity extends AppCompatActivity  {

	private static final String TAG = "WebpagesActivity";
	private static final String HTML_EXTENSION = ".html";
	private ActivityWebpagesBinding binding;
	private WebpageItemAdapter pageItemAdapter;
	private MySettingsManager settingsManager;
	private String ROOT_PATH;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Handler mainHandler = new Handler(Looper.getMainLooper());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityWebpagesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		ROOT_PATH = settingsManager.getRootStoragePath();

		setupRecyclerView();
		setupRefreshButton();

		loadWebpages(); // Initial load
	}

	private void setupRecyclerView() {
		binding.webpagesHosts.setLayoutManager(new LinearLayoutManager(this));
		pageItemAdapter = new WebpageItemAdapter(new ArrayList<>());
		binding.webpagesHosts.setAdapter(pageItemAdapter);
	}

	private void setupRefreshButton() {
		binding.webpagesRefreshButton.setOnClickListener(v -> loadWebpages());
	}

	private void loadWebpages() {
		//Log.d(TAG, "Loading webpages...");
		pageItemAdapter.setWebpageItems(new ArrayList<>()); // Clear the previous items

		if (ROOT_PATH == null || ROOT_PATH.isEmpty()) {
			showError("Error: Invalid storage path.");
			return;
		}

		File rootDir = new File(ROOT_PATH);
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			showError("Error: Invalid storage directory.");
			return;
		}

		executor.execute(() -> {
			List<String> htmlFilesPaths = new ArrayList<>();
			searchRecursive(rootDir, HTML_EXTENSION, htmlFilesPaths);

			mainHandler.post(() -> {
				if (htmlFilesPaths.isEmpty()) {
					Toast.makeText(this, "No HTML files found.", Toast.LENGTH_SHORT).show();
				}
				pageItemAdapter.setWebpageItems(htmlFilesPaths);
				//Log.d(TAG, "Loaded Items: " + htmlFilesPaths.toString());
			});
		});
	}

	private void searchRecursive(File dir, String extension, List<String> files) {
		File[] fileList = dir.listFiles();
		if (fileList == null) {
			return;
		}
		for (File file : fileList) {
			if (file.isDirectory()) {
				searchRecursive(file, extension, files);
			} else if (file.getName().endsWith(extension)) {
				files.add(file.getAbsolutePath().replace(ROOT_PATH + File.separator, ""));
			}
		}
	}

	private void showError(String message) {
		Log.e(TAG, message);
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		executor.shutdownNow(); // Shutdown the executor
	}

	public void loadPage(View v) {
		TextView textView = (TextView) v;
		String filename = textView.getText().toString();
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("loadUrl", true);
		intent.putExtra("url", filename);
		startActivity(intent);
		alert("loading...."+filename);
//		finish();
	}

	private void alert(String msg) {
		Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
	}
}
