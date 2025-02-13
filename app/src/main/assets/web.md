```xml res/layout/activity_webpages.xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:orientation="vertical">

	<Button
		android:id="@+id/webpages_refresh_button"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="@string/refresh" />

	<androidx.recyclerview.widget.RecyclerView
		android:id="@+id/webpages_hosts"
		android:layout_width="match_parent"
		android:layout_height="wrap_content" />

</LinearLayout>
```
```xml res/layout/item_page.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="wrap_content"
	android:gravity="center"
	android:orientation="horizontal"
	android:padding="5dp">

	<TextView
		android:id="@+id/nametext"
		android:layout_width="0dp"
		android:layout_height="wrap_content"
		android:layout_weight="1"
		android:onClick="launch"
		android:text="host" />

	<TextView
		android:id="@+id/size"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="size" />

</LinearLayout>
```
```java java/com/sk/revisit/activities/WebpagesActivity.java
package com.sk.revisit.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.Log;
import com.sk.revisit.R;
import com.sk.revisit.adapter.WebpageItemAdapter;
import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WebpagesActivity extends AppCompatActivity {

	private static final String TAG = "WebpagesActivity";
	private static final String HTML_EXTENSION = ".html";
	ActivityWebpagesBinding binding;
	RecyclerView recyclerView;
	MySettingsManager settingsManager;
	Button webpagesRefreshButton;
	String ROOT_PATH;
	private WebpageItemAdapter pageItemAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityWebpagesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		ROOT_PATH = settingsManager.getRootStoragePath();
		webpagesRefreshButton = binding.webpagesRefreshButton;
		recyclerView = binding.webpagesHosts;
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		// Initialize adapter with empty list
		pageItemAdapter = new WebpageItemAdapter(new ArrayList<>());
		recyclerView.setAdapter(pageItemAdapter);

		loadWebpages(); // Initial load

		webpagesRefreshButton.setOnClickListener(v -> loadWebpages()); // Refresh on button click
	}

	@SuppressLint("ResourceAsColor")
	public void launch(View view) {
		TextView view2 = (TextView) view;
		runOnUiThread(()->{
			view2.setBackgroundColor(R.color.teal_700);
		});
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("loadUrl", true);
		intent.putExtra("url", view2.getText().toString());
		startActivity(intent);
		finish();
		// Toast.makeText(this,"launch working",Toast.LENGTH_SHORT).show();
	}


	private void loadWebpages() {
		Log.d(TAG, "Loading webpages...");
		pageItemAdapter.setWebpageItems(new ArrayList<>()); // Clear the previous items

		if (ROOT_PATH == null || ROOT_PATH.isEmpty()) {
			Log.e(TAG, "Root path is null or empty. Cannot search for files.");
			Toast.makeText(this, "Error: Invalid storage path.", Toast.LENGTH_SHORT).show();
			return;
		}

		File rootDir = new File(ROOT_PATH);
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			Log.e(TAG, "Invalid root directory: " + ROOT_PATH);
			Toast.makeText(this, "Error: Invalid storage directory.", Toast.LENGTH_SHORT).show();
			return;
		}

		List<String> htmlFilesPaths = new ArrayList<>();
		searchRecursive(rootDir, HTML_EXTENSION, htmlFilesPaths);

		if (htmlFilesPaths.isEmpty()) {
			// Log.d(TAG, "No HTML files found.");
			Toast.makeText(this, "No HTML files found.", Toast.LENGTH_SHORT).show();
			return;
		}

		pageItemAdapter.setWebpageItems(htmlFilesPaths);
		// Log.d(TAG, "Loaded Items: " + htmlFilesPaths.toString());
	}

	private void searchRecursive(File dir, String extension, List<String> files) {
		File[] fileList = dir.listFiles();
		if (fileList == null) {
			return; // Nothing to do
		}
		for (File file : fileList) {
			if (file.isDirectory()) {
				searchRecursive(file, extension, files);
			} else if (file.getName().endsWith(extension)) {
				files.add(file.getAbsolutePath().replace(ROOT_PATH + File.separator, ""));
			}
		}
	}
}

```
```java java/com/sk/revisit/adapter/WebpageItemAdapter.java
package com.sk.revisit.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;

import java.util.List;
import java.util.Objects;

public class WebpageItemAdapter extends RecyclerView.Adapter<WebpageItemAdapter.WebpageItemViewHolder> {

	public List<String> webpageFileNames; // Renamed for clarity

	public WebpageItemAdapter(List<String> webpageFileNames) {
		this.webpageFileNames = webpageFileNames;
	}

	public void setWebpageItems(List<String> newWebpageFileNames) {
		// Check if the new list is the same as the old list to avoid unnecessary updates
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

	public static class WebpageItemViewHolder extends RecyclerView.ViewHolder {
		TextView fileNameTextView;

		public WebpageItemViewHolder(@NonNull View itemView) {
			super(itemView);
			fileNameTextView = itemView.findViewById(R.id.nametext);
		}
	}
}
```
