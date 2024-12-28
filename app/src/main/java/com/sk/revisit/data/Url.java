package com.sk.revisit.data;

import android.net.Uri;

public class Url {
    private final String url;
    private boolean isDownloaded;
    private boolean isSelected;
    private boolean isUpdateAvailable;

    public Url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public Uri getUri() {
        return Uri.parse(this.url);
    }

    public boolean isDownloaded() {
        return this.isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.isDownloaded = downloaded;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        isUpdateAvailable = updateAvailable;
    }
}
