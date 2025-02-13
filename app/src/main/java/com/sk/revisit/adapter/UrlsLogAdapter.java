package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.UrlLog;
import com.sk.revisit.databinding.ItemUrllogBinding;

import java.util.List;

public class UrlsLogAdapter extends RecyclerView.Adapter<UrlsLogAdapter.UrlViewHolder> {

	List<UrlLog> urlLogs;
	public UrlsLogAdapter() {
	}

	@NonNull
	@Override
	public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemUrllogBinding binding = ItemUrllogBinding.inflate(LayoutInflater.from(parent.getContext()));
		return new UrlViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
		holder.bind(urlLogs.get(position));
	}

	@Override
	public int getItemCount() {
		return urlLogs.size();
	}

	public static class UrlViewHolder extends RecyclerView.ViewHolder {
		ItemUrllogBinding binding;

		UrlViewHolder(ItemUrllogBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(UrlLog urlLog) {
			binding.urlText.setText(urlLog.url);
			binding.size.setText((int) urlLog.size);
			urlLog.setOnProgressChangeListener((p) -> {
				binding.progress.setProgress((int) p);
			});
		}
	}
}

