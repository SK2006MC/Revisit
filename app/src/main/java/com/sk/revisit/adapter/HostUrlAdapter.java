package com.sk.revisit.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.Host;
import com.sk.revisit.data.Url;

import java.util.List;

public class HostUrlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "HostUrlAdapter";
    private static final int VIEW_TYPE_HOST = 0;
    private static final int VIEW_TYPE_URL = 1;

    private List<Host> hostList;

    public HostUrlAdapter(List<Host> hostList) {
        this.hostList = hostList;
    }

    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
        notifyDataSetChanged();
    }


    private int getHostPositionForUrl(int urlItemPosition) {
        int hostItemCount = 0;
        int hostIndex = -1;
        for (int i = 0; i < hostList.size(); i++) {
            Host host = hostList.get(i);
            hostIndex++; // Increment for the host item itself
            if (host.isExpanded()) {
                if (urlItemPosition > hostIndex && urlItemPosition <= hostIndex + host.getUrls().size()) {
                    return i; // Found the host index for the URL item
                }
                hostIndex += host.getUrls().size();
            }
        }
        return -1; // Should not happen in normal cases, but return -1 to indicate error if needed
    }

    private int getUrlPositionInHost(int urlItemPosition, int hostPosition) {
        if (hostPosition == -1)
            return -1;
        int hostItemCount = 0;
        int urlIndexInHost;
        for (int i = 0; i < hostList.size(); i++) {
            Host host = hostList.get(i);
            hostItemCount++; // For the host item
            if (host.isExpanded()) {
                if (i == hostPosition) {
                    urlIndexInHost = urlItemPosition - hostItemCount; // Calculate URL index within the host's URL list
                    return urlIndexInHost;
                }
                hostItemCount += host.getUrls().size();
            }
        }
        return -1; // Should not happen in normal cases, but return -1 to indicate error if needed
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof Host) {
            return VIEW_TYPE_HOST;
        } else if (item instanceof Url) {
            return VIEW_TYPE_URL;
        } else {
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (Host host : hostList) {
            count++;
            if (host.isExpanded()) {
                count += host.getUrls().size();
            }
        }
        return count;
    }

    private Object getItem(int position) {
        int hostItemCount = 0;
        for (Host host : hostList) {
            if (hostItemCount == position) {
                return host;
            }
            hostItemCount++;
            if (host.isExpanded()) {
                if (position < hostItemCount + host.getUrls().size()) {
                    return host.getUrls().get(position - hostItemCount);
                }
                hostItemCount += host.getUrls().size();
            }
        }
        return null;
    }
}