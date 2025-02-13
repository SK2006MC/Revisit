package com.sk.revisit.data;

public class Url {
	public final String url;
	public long size;
	public double progress;
	public boolean isDownloaded;
	public boolean isSelected;
	public boolean isUpdateAvailable;
	public OnProgressChangeListener listener;

	public Url(String url) {
		this.url = url;
		this.progress = 0; // Initialize progress to 0
		this.size = 0;
		this.isDownloaded = false;
		this.isSelected = false;
		this.isUpdateAvailable = false;
	}

	public void setProgress(double p){
		this.progress = p;
		if(listener!=null){
			listener.onChange(p);
		}
	}

	public void setOnProgressChangeListener(OnProgressChangeListener listener) {
		if(listener==null){
			return;
		}
		if(this.listener==null){
			this.listener = listener;
		}
	}

	public interface OnProgressChangeListener{
		void onChange(double p);
	}
}