```xml res/layout/activity_download.xml
<?xml version="1.0" encoding="utf-8" ?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:orientation="vertical"
	android:padding="4dp">

	<LinearLayout
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:orientation="horizontal">

		<Button
			android:id="@+id/refresh_button"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:text="Refresh"/>
		<Button
			android:id="@+id/calc"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:text="@string/calculate_size" />

		<TextView
			android:id="@+id/total"
			style="@style/AppTheme"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:layout_marginStart="10dp"
			android:text="@string/total"
			android:textSize="20sp" />

	</LinearLayout>


	<LinearLayout
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:orientation="horizontal">

		<Button
			android:id="@+id/button_download"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:text="@string/download" />

		<TextView
			android:id="@+id/prog"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:text="prog"
			android:visibility="gone" />

		<TextView
			android:id="@+id/completed"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:text="completed"
			android:visibility="gone" />

		<TextView
			android:id="@+id/downloadStatus"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:text="status"
			android:visibility="gone" />

	</LinearLayout>

	<androidx.recyclerview.widget.RecyclerView
		android:id="@+id/urls"
		android:layout_width="match_parent"
		android:layout_height="match_parent" />

</LinearLayout>
```
```xml res/layout/item_url.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="wrap_content"
	android:gravity="center"
	android:orientation="horizontal">

	<CheckBox
		android:id="@+id/url_checkbox"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content" />

	<TextView
		android:id="@+id/url_text"
		android:layout_width="0dp"
		android:layout_height="wrap_content"
		android:layout_weight="1" />

	<TextView
		android:id="@+id/url_size"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content" />

	<ProgressBar
		android:id="@+id/url_progressbar"
		style="@style/Widget.AppCompat.ProgressBar.Horizontal"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content" />

</LinearLayout>
```
```java java/com/sk/revisit/activities/DownloadActivity.java
package com.sk.revisit.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.Log;
import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.adapter.UrlAdapter;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ActivityDownloadBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DownloadActivity extends AppCompatActivity {

	private static final String TAG = "DownloadActivity";
	private final Set<String> urlsStr = new HashSet<>();
	private ActivityDownloadBinding binding;
	private MyUtils myUtils;
	private MySettingsManager settingsManager;
	UrlAdapter urlAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDownloadBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		myUtils = new MyUtils(this, settingsManager.getRootStoragePath());

		loadUrisFromFile();
		initUI();
		initRecyclerView();
	}

	void initRecyclerView() {
		List<Url> urlList = new ArrayList<>();

		for (String urlStr : urlsStr) {
			urlList.add(new Url(urlStr));
		}

		urlAdapter = new UrlAdapter(urlList);
		binding.urls.setAdapter(urlAdapter);

		binding.urls.setLayoutManager(new LinearLayoutManager(this));
		DividerItemDecoration decoration=new DividerItemDecoration(
				binding.urls.getContext(),
				LinearLayoutManager.VERTICAL
		);
		decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this,R.drawable.divider)));
		binding.urls.addItemDecoration(decoration);
	}

	public void refreshUrls(){
		urlAdapter.notifyDataSetChanged();
	}

	private void loadUrisFromFile() {
		String filePath = settingsManager.getRootStoragePath() + File.separator + "req.txt";

		File file = new File(filePath);

		if (!file.exists()) {
			Log.e(TAG, "req.txt not found at: " + filePath);
			alert("req.txt not found at: " + filePath);
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String url = line.trim();
				urlsStr.add(url);
			}
		} catch (IOException e) {
			alert("Error reading req.txt");
		}
	}

	void download(){

	}
	void initUI() {
		binding.total.setText("0 B");
		binding.refreshButton.setOnClickListener(v->{
			refreshUrls();
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myUtils.shutdown();
	}

	void alert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
```java java/com/sk/revisit/data/Url.java
package com.sk.revisit.data;

public class Url {
    private final String url;
    private long size;
    private int progress;
    private boolean isDownloaded;
    private boolean isSelected;
    private boolean isUpdateAvailable;
    private progressListener listener;

    public Url(String url) {
        this.url = url;
        this.progress = 0; // Initialize progress to 0
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isDownloaded() {
        return this.isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.isDownloaded = downloaded;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        isUpdateAvailable = updateAvailable;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress >= 0 && progress <= 100) {
            this.progress = progress;
            if (listener != null) {
                listener.onProgressChanged(progress);
            }
        }
    }

    public void setProgressListener(progressListener listener) {
        this.listener = listener;
    }

    public interface progressListener {
        void onProgressChanged(int progress);
    }
}
```
