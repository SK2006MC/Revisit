package com.sk.revisit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.adapter.WebpageItemAdapter;
import com.sk.revisit.data.ItemPage;
import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.managers.MySettingsManager;
import com.sk.revisit.managers.WebpageRepository; // Import the new repository

import java.util.ArrayList;
import java.util.List;

public class WebpagesActivity extends BaseActivity implements WebpageRepository.Callback {

    private ActivityWebpagesBinding binding;
    private WebpageItemAdapter pageItemAdapter;
    private WebpageRepository webpageRepository;

    // UI Feedback Handlers
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_DELAY = 300; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebpagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MySettingsManager settingsManager = new MySettingsManager(this);
        String rootPath = settingsManager.getRootStoragePath();

        // Initialize Repository
        webpageRepository = new WebpageRepository(this, rootPath);

        initUi();
        loadWebpages();
    }

    private void initUi() {
        binding.webpagesHosts.setLayoutManager(new LinearLayoutManager(this));
        pageItemAdapter = new WebpageItemAdapter(new ArrayList<>());
        binding.webpagesHosts.setAdapter(pageItemAdapter);

        binding.webpagesRefreshButton.setOnClickListener(v -> loadWebpages());

        // Apply search debounce logic
        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                String keywords = editable.toString();
                searchRunnable = () -> filterPagesByKeywords(keywords);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
            }
        });
    }

    private void loadWebpages() {
        // Show loading indicator (UX Improvement)
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.webpagesHosts.setVisibility(View.GONE);
        pageItemAdapter.setWebPages(new ArrayList<>()); // Clear old data

        try {
            webpageRepository.loadWebpages(this);
        } catch (Exception e) {
            onError(e.toString());
            // Hide loading indicator on immediate failure
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    // --- WebpageRepository.Callback Implementation ---

    @Override
    public void onSuccess(List<ItemPage> pages) {
        // Post back to main thread (Repository handles this, but good practice to ensure)
        runOnUiThread(() -> {
            pageItemAdapter.setWebPages(pages);

            if (pages.isEmpty()) {
                alert("No HTML files found in the root directory.");
            }

            // Hide loading indicator and show list
            binding.progressBar.setVisibility(View.GONE);
            binding.webpagesHosts.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            alert(message);
            // Hide loading indicator on error
            binding.progressBar.setVisibility(View.GONE);
            binding.webpagesHosts.setVisibility(View.VISIBLE); // Show list even if empty
        });
    }

    // --- Other Methods ---

    void filterPagesByKeywords(String keywords) {
        pageItemAdapter.filter(keywords);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webpageRepository.shutdown(); // Shut down repository executor

        // Clean up search handler
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    public void loadPage(View v) {
        // Assuming the ViewHolder/Adapter sets the tag or uses the View structure
        // to find the filename correctly. If 'v' is the TextView itself, this works.
        if (v instanceof TextView) {
            String filename = ((TextView) v).getText().toString();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("loadUrl", true);
            intent.putExtra("url", filename);
            startActivity(intent);
            alert("loading..  " + filename);
            getRevisitApp().getLastActivity().finish();
            finish();
        }
    }
}