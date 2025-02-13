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

	public class WebpageItemViewHolder extends RecyclerView.ViewHolder {
		TextView fileNameTextView;

		public WebpageItemViewHolder(@NonNull View itemView) {
			super(itemView);
			fileNameTextView = itemView.findViewById(R.id.nametext);
		}
	}
}
