package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ItemUrlBinding;
import com.sk.revisit.log.Log;

import java.util.List;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlViewHolder> {

	public static final String TAG = UrlAdapter.class.getSimpleName();
	private final List<Url> urlList;

	public UrlAdapter(List<Url> urlList) {
		this.urlList = urlList;
	}

	@NonNull
	@Override
	public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemUrlBinding urlBinding = ItemUrlBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
		return new UrlViewHolder(urlBinding);
	}

	@Override
	public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
		holder.bind(urlList.get(position));
	}

	@Override
	public int getItemCount() {
		return urlList.size();
	}

	public static class UrlViewHolder extends RecyclerView.ViewHolder implements  Url.OnProgressChangeListener {

		ItemUrlBinding binding;
		Url currentUrl;

		public UrlViewHolder(@NonNull ItemUrlBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
			binding.urlCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
				if(currentUrl != null){
					 currentUrl.isSelected = isChecked;
				}
			});
		}

		public void bind(@NonNull Url url){
			currentUrl = url;
			binding.urlTextview.setText(url.url);
			binding.sizeTextview.setText(url.size > 0 ? url.size + " bytes" : "Calculating...");
			binding.urlCheckbox.setChecked(url.isSelected);
			url.setOnProgressChangeListener(this);
		}


		@Override
		public void onChange(double p) {
			binding.progressBar.setProgress((int) p);
		}
	}
}