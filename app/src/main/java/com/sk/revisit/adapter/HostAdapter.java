package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.Host;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ItemHostBinding;

import java.util.List;

public class HostAdapter extends RecyclerView.Adapter<HostAdapter.HostViewHolder> {

    private List<Host> hosts;

    public HostAdapter(List<Host> hosts) {
        this.hosts = hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHostBinding binding = ItemHostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        Host host = hosts.get(position);
        holder.bind(host);
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    public static class HostViewHolder extends RecyclerView.ViewHolder {
        private final ItemHostBinding binding;

        public HostViewHolder(@NonNull ItemHostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Host host) {
            binding.hostText.setText(host.getName());
            // binding.expandhost.setOnClickListener(v -> host.setIsExpanded(!host.getIsExpanded()));
            // binding.hostCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> host.setIsSelected(isChecked));
            binding.hostSize.setText(String.valueOf(host.getTotalSize()));
            binding.hostProgressbar.setProgress(0);

            List<Url> urls = host.getUrls();
            UrlAdapter urlAdapter = new UrlAdapter(urls);
            binding.urls.setAdapter(urlAdapter);
        }
    }
}
