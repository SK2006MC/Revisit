```xml
<!-- res/layout/activity_webpages.xml -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:id="@+id/webpages_refresh_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/refresh"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        android:layout_marginTop="8dp"
        android:layout_marginStart="8dp"/>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/webpages_hosts"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toBottomOf="@+id/webpages_refresh_button"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        android:layout_marginTop="8dp"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

```xml
<!-- res/layout/item_page.xml -->
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="4dp"
    app:cardCornerRadius="4dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp"
        android:gravity="center_vertical">

        <TextView
            android:id="@+id/nametext"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="host"
            android:textSize="16sp"
            android:textColor="@android:color/black"
            android:padding="8dp"
            android:clickable="true"
            android:focusable="true"
            android:foreground="?android:attr/selectableItemBackground"/>

        <TextView
            android:id="@+id/size"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="size"
            android:textSize="14sp"
            android:padding="8dp"/>

    </LinearLayout>

</androidx.cardview.widget.CardView>
```

```java
package com.sk.revisit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.Log;
import com.sk.revisit.adapter.WebpageItemAdapter;
import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebpagesActivity extends AppCompatActivity implements WebpageItemAdapter.OnItemClickListener {

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
        pageItemAdapter.setOnItemClickListener(this); // Set the listener
        binding.webpagesHosts.setAdapter(pageItemAdapter);
    }

    private void setupRefreshButton() {
        binding.webpagesRefreshButton.setOnClickListener(v -> loadWebpages());
    }

    private void loadWebpages() {
        Log.d(TAG, "Loading webpages...");
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
                Log.d(TAG, "Loaded Items: " + htmlFilesPaths.toString());
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

    @Override
    public void onItemClick(String filename) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("loadUrl", true);
        intent.putExtra("url", filename);
        startActivity(intent);
        //Consider not finishing the activity
        //finish();
    }
}
```

```java
package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;

import java.util.List;
import java.util.Objects;

public class WebpageItemAdapter extends RecyclerView.Adapter<WebpageItemAdapter.WebpageItemViewHolder> {

    private List<String> webpageFileNames;
    private OnItemClickListener listener;

    public WebpageItemAdapter(List<String> webpageFileNames) {
        this.webpageFileNames = webpageFileNames;
    }

    public void setWebpageItems(List<String> newWebpageFileNames) {
        if (!Objects.equals(this.webpageFileNames, newWebpageFileNames)) {
            this.webpageFileNames = newWebpageFileNames;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public WebpageItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);
        return new WebpageItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WebpageItemViewHolder holder, int position) {
        String webpageFileName = webpageFileNames.get(position);
        holder.fileNameTextView.setText(webpageFileName);
    }

    @Override
    public int getItemCount() {
        return webpageFileNames.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String filename);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class WebpageItemViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;

        public WebpageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.nametext);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(webpageFileNames.get(position));
                }
            });
        }
    }
}
```
