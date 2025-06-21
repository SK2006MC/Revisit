package com.sk.revisit.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.GVars;
import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.Revisit;
import com.sk.revisit.adapter.UrlAdapter;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ActivityDownloadBinding;
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

public class DownloadActivity extends BaseActivity {

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

        urlsAdapter = new UrlAdapter(urlsList);
        initUI();
        loadUrlsFromFile();
    }

    private void loadUrlsFromFile() {
        urlsStr.clear();
        urlsList.clear();
        urlsAdapter.notifyDataSetChanged();

        String filePath = settingsManager.getRootStoragePath() + File.separator + GVars.reqFileName;
        File reqFile = new File(filePath);

        if (!reqFile.exists()) {
            Log.e(TAG, GVars.reqFileName + " not found at: " + filePath);
            alert(GVars.reqFileName + " not found at: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(reqFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String urlStr = line.trim();
                urlsStr.add(urlStr);
                Url url = new Url(urlStr);
                urlsList.add(url);
                urlsAdapter.notifyItemInserted(urlsList.size() - 1);
            }
        } catch (IOException e) {
            alert("Error reading " + filePath);
            Log.e(TAG, "Error reading " + filePath, e);
        }

        saveToFile(urlsStr, reqFile);
    }

    private void initUI() {
        binding.totalSizeTextview.setText(getString(R.string.total));
        binding.refreshButton.setOnClickListener(v -> loadUrlsFromFile());

        binding.calcButton.setOnClickListener(v -> calculateTotalSize());
        binding.downloadButton.setOnClickListener(v -> downloadSelectedUrls());

        binding.urlsRecyclerview.setAdapter(urlsAdapter);
        binding.urlsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decoration = new DividerItemDecoration(
                binding.urlsRecyclerview.getContext(),
                LinearLayoutManager.VERTICAL
        );
        decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
        binding.urlsRecyclerview.addItemDecoration(decoration);
    }

    private void downloadSelectedUrls() {
        List<Integer> selectedPositions = new ArrayList<>();
        for (int i = 0; i < urlsList.size(); i++) {
            if (urlsList.get(i).isSelected) {
                selectedPositions.add(i);
            }
        }

        if (selectedPositions.isEmpty()) {
            alert("No URLs selected for download.");
            return;
        }

        for (int position : selectedPositions) {
            Url url = urlsList.get(position);
            myUtils.download(Uri.parse(url.url), new MyUtils.DownloadListener() {
                private void notifyUpdate() {
                    mainHandler.post(() -> urlsAdapter.notifyItemChanged(position));
                }

                @Override
                public void onStart(Uri uri, long contentLength) {
                    url.size = contentLength;
                }

                @Override
                public void onSuccess(File file, Headers headers) {
                    url.isDownloaded = true;
                    url.setProgress(100);
                    notifyUpdate();
                }

                @Override
                public void onProgress(double p) {
                    url.setProgress(p);
                    notifyUpdate();
                }

                @Override
                public void onFailure(Exception e) {
                    url.isDownloaded = false;
                    notifyUpdate();
                }

                @Override
                public void onEnd(File file) {
                }
            });
        }
    }

    private void calculateTotalSize() {
        if (!Revisit.isNetworkAvailable) {
            alert("No network available!");
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
                mainHandler.post(() -> binding.totalSizeTextview.setText(String.format(Locale.ENGLISH, format, totalSize.get())));
            }
        });
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
}