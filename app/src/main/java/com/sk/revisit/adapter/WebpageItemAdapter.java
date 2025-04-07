package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;

import java.util.ArrayList;
import java.util.List;

public class WebpageItemAdapter extends RecyclerView.Adapter<WebpageItemAdapter.WebpageItemViewHolder> {

	private List<String> webpageFileNames;
	private List<String> originalWebpageFileNames;

	public WebpageItemAdapter(List<String> webpageFileNames) {
		this.webpageFileNames = webpageFileNames;
		this.originalWebpageFileNames = new ArrayList<>(webpageFileNames);
	}

	public void setWebpageItems(List<String> newWebpageFileNames) {
		WebpageItemDiffCallback diffCallback = new WebpageItemDiffCallback(this.webpageFileNames, newWebpageFileNames);
		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

		this.originalWebpageFileNames = new ArrayList<>(newWebpageFileNames);
		this.webpageFileNames = newWebpageFileNames;
		diffResult.dispatchUpdatesTo(this);
	}

	public void filter(String query) {
		List<String> filteredList;
		if (query.trim().isEmpty()) {
			filteredList = originalWebpageFileNames;
		} else {
			filteredList = new ArrayList<>();
			for (String item : originalWebpageFileNames) {
				if (item.toLowerCase().contains(query.toLowerCase())) {
					filteredList.add(item);
				}
			}
		}
		setWebpageItems(filteredList);
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
		public TextView fileNameTextView;

		public WebpageItemViewHolder(View itemView) {
			super(itemView);
			fileNameTextView = itemView.findViewById(R.id.nametext);
		}
	}

	public static class WebpageItemDiffCallback extends DiffUtil.Callback {

		private final List<String> oldList;
		private final List<String> newList;

		public WebpageItemDiffCallback(List<String> oldList, List<String> newList) {
			this.oldList = oldList;
			this.newList = newList;
		}

		@Override
		public int getOldListSize() {
			return oldList.size();
		}

		@Override
		public int getNewListSize() {
			return newList.size();
		}

		@Override
		public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
			return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
		}

		@Override
		public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
			return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
		}
	}
}