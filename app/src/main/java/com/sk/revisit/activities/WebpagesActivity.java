package com.sk.revisit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.adapter.WebpageItemAdapter;
import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.log.Log;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebpagesActivity extends AppCompatActivity {

	private static final String TAG = "WebpagesActivity";
	private static final String HTML_EXTENSION = ".html";
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private ActivityWebpagesBinding binding;
	private WebpageItemAdapter pageItemAdapter;
	private String ROOT_PATH;
	private List<String> htmlFilesPaths;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityWebpagesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		MySettingsManager settingsManager = new MySettingsManager(this);
		ROOT_PATH = settingsManager.getRootStoragePath();

		setupRecyclerView();
		setupRefreshButton();

		loadWebpages();

		EditText searchBar = binding.searchBar;
		searchBar.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				// Not needed for this example
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				// Not needed for this example
			}

			@Override
			public void afterTextChanged(Editable editable) {
				filterPagesByKeywords(editable.toString());
			}
		});
	}

	void filterPagesByKeywords(String keywords) {
		pageItemAdapter.filter(keywords);
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
		pageItemAdapter.setWebpageItems(new ArrayList<>());

		if (ROOT_PATH == null || ROOT_PATH.isEmpty()) {
			alert("Error: Invalid storage path.");
			return;
		}

		File rootDir = new File(ROOT_PATH);
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			alert("Error: Invalid storage directory.");
			return;
		}

		executor.execute(() -> {
			htmlFilesPaths = new ArrayList<>();
			searchRecursive(rootDir, HTML_EXTENSION, htmlFilesPaths);
			mainHandler.post(() -> {
				if (htmlFilesPaths.isEmpty()) {
					Toast.makeText(this, "No HTML files found.", Toast.LENGTH_SHORT).show();
				}
				pageItemAdapter.setWebpageItems(htmlFilesPaths);
			});
		});
	}

	private void searchRecursive(@NonNull File dir, String extension, List<String> files) {
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

	private void alert(String message) {
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
		alert("loading..  " + filename);
		finish();
	}
}