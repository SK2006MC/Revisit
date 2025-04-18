package com.sk.revisit.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.adapter.UrlAdapter;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ActivityDownloadBinding;
import com.sk.revisit.log.Log;
import com.sk.revisit.managers.MySettingsManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadActivity extends AppCompatActivity {

	private static final String TAG = "DownloadActivity";
	final String format = "Total Size: %d bytes";
	private final Set<String> urlsStr = new HashSet<>();
	private final List<Url> urlsList = new ArrayList<>();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private ActivityDownloadBinding binding;
	private MyUtils myUtils;
	private MySettingsManager settingsManager;
	private UrlAdapter urlsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDownloadBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		myUtils = new MyUtils(this, settingsManager.getRootStoragePath());

		loadUrlsFromFile();
		initUI();
	}

	public void refresh() {
		urlsAdapter.notifyDataSetChanged();
	}

	private void initRecyclerView() {
		binding.urlsRecyclerview.setAdapter(urlsAdapter);
		binding.urlsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
		DividerItemDecoration decoration = new DividerItemDecoration(
				binding.urlsRecyclerview.getContext(),
				LinearLayoutManager.VERTICAL
		);
		decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
		binding.urlsRecyclerview.addItemDecoration(decoration);
	}

	private void loadUrlsFromFile() {
		urlsStr.clear();
		String filePath = settingsManager.getRootStoragePath() + File.separator + "req.txt";

		File reqFile = new File(filePath);
		if (!reqFile.exists()) {
			Log.e(TAG, "req.txt not found at: " + filePath);
			showAlert("req.txt not found at: " + filePath);
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(reqFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String url = line.trim();
				urlsStr.add(url);
			}
		} catch (IOException e) {
			showAlert("Error reading req.txt");
		}

		for (String urlStr : urlsStr) {
			urlsList.add(new Url(urlStr));
		}
		saveToFile(urlsStr, reqFile);
		urlsAdapter = new UrlAdapter(urlsList);
	}

	void saveToFile(Set<String> urls, File file) {
		myUtils.executorService.execute(() -> {
			try {
				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
				for (String url : urls) {
					fileWriter.write(url);
					fileWriter.newLine();
				}
				fileWriter.close();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		});
	}

	private void downloadSelectedUrls() {
		List<Url> selectedUrls = new ArrayList<>();
		for (Url url : urlsList) {
			if (url.isSelected) {
				selectedUrls.add(url);
			}
		}

		if (selectedUrls.isEmpty()) {
			showAlert("No URLs selected for download.");
			return;
		}

		for (Url url : selectedUrls) {
			myUtils.download(Uri.parse(url.url), new MyUtils.DownloadListener() {
				@Override
				public void onStart(Uri uri, long contentLength) {
					url.size = contentLength;
				}

				@Override
				public void onSuccess(File file, Headers headers) {
					url.isDownloaded = true;
					url.setProgress(100);
				}

				@Override
				public void onProgress(double p) {
					url.setProgress(p);
				}

				@Override
				public void onFailure(Exception e) {
					url.isDownloaded = false;
				}

				@Override
				public void onEnd(File file) {
				}
			});
		}
	}

	private void calculateTotalSize() {
		if (!MyUtils.isNetworkAvailable) {
			showAlert("No network available!");
			return;
		}

		myUtils.executorService.execute(() -> {
			AtomicLong totalSize = new AtomicLong(0);
			for (Url url : urlsList) {
				Request request = new Request.Builder().head().url(url.url).build();
				try {
					Response response = myUtils.client.newCall(request).execute();
					assert response.body() != null;
					if (response.isSuccessful()) {
						url.size = response.body().contentLength();
						totalSize.addAndGet(url.size);
					} else {
						url.size = -1;
					}
					mainHandler.post(() -> urlsAdapter.notifyItemChanged(urlsList.indexOf(url)));
				} catch (IOException e) {
					Log.e(TAG, " ", e);
				}
			}
			mainHandler.post(() -> binding.totalSizeTextview.setText(String.format(Locale.ENGLISH, format, totalSize)));
		});
	}

	private void initUI() {
		binding.totalSizeTextview.setText(getString(R.string.total));
		binding.refreshButton.setOnClickListener(v -> {
			loadUrlsFromFile();
			refresh();
		});
		binding.calcButton.setOnClickListener(v -> calculateTotalSize());
		binding.downloadButton.setOnClickListener(v -> downloadSelectedUrls());
		initRecyclerView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myUtils.shutdown();
	}

	private void showAlert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}