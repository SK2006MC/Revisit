package com.sk.revisit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.log.Log;
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