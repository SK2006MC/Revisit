```xml res/layout/activity_log.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:orientation="vertical">

	<LinearLayout
		android:orientation="horizontal"
		android:layout_width="match_parent"
		android:layout_height="wrap_content">

		<TextView
			style="@style/TextAppearance.AppCompat.Headline"
			android:layout_width="0dp"
			android:layout_height="wrap_content"
			android:layout_weight="1"
			android:text="Log" />

		<ImageButton
			android:id="@+id/refresh_button"
			android:contentDescription="Refresh logs"
			android:src="@drawable/baseline_refresh_24"
			android:layout_width="match_parent"
			android:layout_height="wrap_content" />

	</LinearLayout>

	<androidx.recyclerview.widget.RecyclerView
		android:id="@+id/logs"
		android:layout_width="match_parent"
		android:layout_height="wrap_content" />
</LinearLayout>
```
```xml res/layout/item_log.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="wrap_content"
	android:padding="5dp"
	android:orientation="horizontal">

	<TextView
		android:paddingEnd="3dp"
		android:id="@+id/tag"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="@string/item_log_tag" />

	<TextView
		android:paddingStart="3dp"
		android:paddingEnd="5dp"
		android:id="@+id/msg"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="@string/item_log_msg" />

	<ScrollView
		android:paddingStart="3dp"
		android:layout_width="match_parent"
		android:layout_height="wrap_content">

		<TextView
			android:id="@+id/exception"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:text="@string/item_log_exception" />

	</ScrollView>
</LinearLayout>
```
```java java/com/sk/revisit/activities/LogActivity.java
package com.sk.revisit.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.ListAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.Log;
import com.sk.revisit.R;
import com.sk.revisit.adapter.LogRecyclerAdapter;
import com.sk.revisit.databinding.ActivityLogBinding;

public class LogActivity extends AppCompatActivity {

	ActivityLogBinding binding;
	LogRecyclerAdapter adapter;


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		binding = ActivityLogBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		adapter = new LogRecyclerAdapter(Log.getLogs());
		binding.logs.setLayoutManager(new LinearLayoutManager(this));

		DividerItemDecoration decoration = new DividerItemDecoration(
				binding.logs.getContext(),
				LinearLayoutManager.VERTICAL
		);

		decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));

		binding.logs.addItemDecoration(decoration);

		binding.logs.setAdapter(adapter);

		binding.refreshButton.setOnClickListener(v -> loadLogs());
	}

	private void loadLogs() {
		adapter.notifyDataSetChanged();
	}
}
```
```java java/com/sk/revisit/adapter/LogRecyclerAdapter.java
package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;
import com.sk.revisit.databinding.ItemLogBinding;

import java.util.ArrayList;
import java.util.List;

public class LogRecyclerAdapter extends RecyclerView.Adapter<LogRecyclerAdapter.LogViewHolder> {

	List<String[]> logs;
	public LogRecyclerAdapter(List<String[]> logs) {
		this.logs = logs;
	}

	@NonNull
	@Override
	public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemLogBinding binding = ItemLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new LogViewHolder(binding);
	}

	@Override
	public int getItemCount() {
		return logs.size();
	}

	@Override
	public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
		holder.bind(logs.get(position));
	}

	public static class LogViewHolder extends RecyclerView.ViewHolder {
		private final ItemLogBinding binding;

		public LogViewHolder(@NonNull ItemLogBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(String[] log) {
			binding.tag.setText(log[0]);
			binding.msg.setText(log[1]);
			try {
				binding.exception.setText(log[2]);
			} catch (Exception e) {
				binding.exception.setText(R.string.no_exception_given);
			}
		}
	}
}
```
