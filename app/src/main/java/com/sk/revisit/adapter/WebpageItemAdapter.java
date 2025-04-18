package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.ItemPage;
import com.sk.revisit.databinding.ItemPageBinding;

import java.util.ArrayList;
import java.util.List;

public class WebpageItemAdapter extends RecyclerView.Adapter<WebpageItemAdapter.WebpageItemViewHolder> {

    private List<ItemPage> webPages;
    private List<ItemPage> webPagesOrg;

    public WebpageItemAdapter(List<ItemPage> webPages) {
        this.webPagesOrg = webPages;
        this.webPages = webPages;
    }

    public void setWebPagesOrg(List<ItemPage> webPagesOrg) {
        this.webPagesOrg = webPagesOrg;
        this.webPages = webPagesOrg;
    }

    public void sort() {
        //webPages.sort(ItemPage::getSize);
    }

    public void setWebPages(List<ItemPage> newWebpages) {
        WebpageItemDiffCallback diffCallback = new WebpageItemDiffCallback(webPagesOrg, newWebpages);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        webPages = newWebpages;
        diffResult.dispatchUpdatesTo(this);
    }

    public void filter(String query) {
        List<ItemPage> filteredList;
        if (query.trim().isEmpty()) {
            filteredList = webPagesOrg;
        } else {
            filteredList = new ArrayList<>();
            for (ItemPage page : webPagesOrg) {
                if (page.fileName.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(page);
                }
            }
        }
        setWebPages(filteredList);
    }

    @NonNull
    @Override
    public WebpageItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPageBinding binding = ItemPageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WebpageItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WebpageItemViewHolder holder, int position) {
        ItemPage page = webPages.get(position);
        holder.bind(page);
    }

    @Override
    public int getItemCount() {
        return webPages.size();
    }

    public static class WebpageItemViewHolder extends RecyclerView.ViewHolder {
        ItemPageBinding binding;

        public WebpageItemViewHolder(ItemPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemPage page) {
            binding.host.setText(page.host);
            binding.size.setText(page.sizeStr);
            binding.nametext.setText(page.fileName);
        }
    }

    public static class WebpageItemDiffCallback extends DiffUtil.Callback {

        private final List<ItemPage> oldList;
        private final List<ItemPage> newList;

        public WebpageItemDiffCallback(List<ItemPage> oldList, List<ItemPage> newList) {
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