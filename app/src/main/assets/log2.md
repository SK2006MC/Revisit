```xml
res/layout/activity_log.xml
```
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:padding="8dp">

        <TextView
            style="@style/TextAppearance.AppCompat.Headline"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Log" />

        <ImageButton
            android:id="@+id/refresh_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Refresh logs"
            android:padding="8dp"
            android:src="@drawable/baseline_refresh_24" />

    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/logs_recycler_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
</LinearLayout>
```
```xml
res/layout/item_log.xml
```
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="8dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <TextView
            android:id="@+id/log_tag_text_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textAppearance="?attr/textAppearanceBodyMedium" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:paddingStart="8dp"
            android:paddingEnd="8dp"
            android:text="-" />

        <TextView
            android:id="@+id/log_message_text_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textAppearance="?attr/textAppearanceBodyMedium" />
    </LinearLayout>

    <TextView
        android:id="@+id/log_exception_text_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:textAppearance="?attr/textAppearanceBodySmall" />

</LinearLayout>
```
```java
java/com/sk/revisit/activities/LogActivity.java
```
```java
package com.sk.revisit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.Log;
import com.sk.revisit.R;
import com.sk.revisit.adapter.LogRecyclerAdapter;
import com.sk.revisit.databinding.ActivityLogBinding;

import java.util.List;

public class LogActivity extends AppCompatActivity {

    private ActivityLogBinding binding;
    private LogRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        binding.refreshButton.setOnClickListener(v -> refreshLogs());
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.logsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        List<String[]> logs = Log.getLogs();
        adapter = new LogRecyclerAdapter(logs);
        recyclerView.setAdapter(adapter);
    }

    private void refreshLogs() {
        List<String[]> newLogs = Log.getLogs();
        adapter.setLogs(newLogs);
        adapter.notifyDataSetChanged();
    }
}
```
```java
java/com/sk/revisit/adapter/LogRecyclerAdapter.java
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

public class LogRecyclerAdapter extends RecyclerView.Adapter<LogRecyclerAdapter.LogViewHolder> {

    private List<String[]> logs;

    public LogRecyclerAdapter(List<String[]> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        String[] log = logs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void setLogs(List<String[]> logs) {
        this.logs = logs;
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        private final TextView logTagTextView;
        private final TextView logMessageTextView;
        private final TextView logExceptionTextView;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logTagTextView = itemView.findViewById(R.id.log_tag_text_view);
            logMessageTextView = itemView.findViewById(R.id.log_message_text_view);
            logExceptionTextView = itemView.findViewById(R.id.log_exception_text_view);
        }

        void bind(String[] log) {
            logTagTextView.setText(log[0]);
            logMessageTextView.setText(log[1]);

            if (log.length > 2 && log[2] != null && !log[2].isEmpty()) {
                logExceptionTextView.setText(log[2]);
                logExceptionTextView.setVisibility(View.VISIBLE);
            } else {
                logExceptionTextView.setVisibility(View.GONE);
            }
        }
    }
}
```
