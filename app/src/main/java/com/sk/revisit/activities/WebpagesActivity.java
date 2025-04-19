package com.sk.revisit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.adapter.WebpageItemAdapter;
import com.sk.revisit.data.ItemPage;
import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.log.Log;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class WebpagesActivity extends AppCompatActivity {

	private static final String TAG = "WebpagesActivity";
	private static final String HTML_EXTENSION = ".html";
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private ActivityWebpagesBinding binding;
	private WebpageItemAdapter pageItemAdapter;
	private String ROOT_PATH;
	private List<String> htmlFilesPaths;
	private List<ItemPage> webPages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityWebpagesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		MySettingsManager settingsManager = new MySettingsManager(this);
		ROOT_PATH = settingsManager.getRootStoragePath();

		initUi();
		loadWebpages();
	}

	private void initUi() {
		binding.webpagesHosts.setLayoutManager(new LinearLayoutManager(this));
		pageItemAdapter = new WebpageItemAdapter(new ArrayList<>());
		binding.webpagesHosts.setAdapter(pageItemAdapter);

		binding.webpagesRefreshButton.setOnClickListener(v -> loadWebpages());
		binding.searchBar.addTextChangedListener(new TextWatcher() {
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

	private void loadWebpages() {
		pageItemAdapter.setWebPages(new ArrayList<>());

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
			webPages = new ArrayList<>();
			pageItemAdapter.setWebPagesOrg(webPages);
			searchRecursive(rootDir, HTML_EXTENSION, htmlFilesPaths);
			int i = 0;
			for (String htmlFile : htmlFilesPaths) {
				ItemPage page = new ItemPage();
				page.host = htmlFile.split("/")[0];
				page.fileName = htmlFile;
				page.size = calcSize(ROOT_PATH + File.separator + htmlFile);
				page.sizeStr = Formatter.formatFileSize(this, page.size);
				webPages.add(page);
				notifyAdapter(i);
				i++;
			}
			mainHandler.post(() -> {
				if (htmlFilesPaths.isEmpty()) {
					Toast.makeText(this, "No HTML files found.", Toast.LENGTH_SHORT).show();
				}
			});
		});
	}

	void notifyAdapter(int i) {
		runOnUiThread(() -> pageItemAdapter.notifyItemInserted(i));
	}

	private long calcSize(String htmlFile) {
		File file = new File(htmlFile);
		try {
			return getFolderSize(file.getParent());
		} catch (Exception e) {
			return -1;
		}
	}

	long getFolderSize(String folderPath) throws IOException {
		Path folder = Paths.get(folderPath);
		AtomicLong size = new AtomicLong(0);
		try (Stream<Path> walk = Files.walk(folder)) {
			walk.parallel()
					.filter(Files::isRegularFile)
					.forEach(path -> size.addAndGet(path.toFile().length()));
		}
		return size.get();
	}

	void filterPagesByKeywords(String keywords) {
		pageItemAdapter.filter(keywords);
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