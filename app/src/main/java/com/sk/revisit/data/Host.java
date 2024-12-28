package com.sk.revisit.data;

import java.util.ArrayList;
import java.util.List;

public class Host {
    private String name;
    private boolean isSelected;
    private boolean isExpanded;
    private List<Url> urls;
    private long totalSize; // For aggregated size of URLs under this host

    public Host(String name) {
        this.name = name;
        this.isSelected = false;
        this.isExpanded = false;
        this.urls = new ArrayList<>();
        this.totalSize = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public List<Url> getUrls() {
        return urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void addUrl(Url url) {
        this.urls.add(url);
    }
}