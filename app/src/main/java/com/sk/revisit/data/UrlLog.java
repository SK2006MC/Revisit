package com.sk.revisit.data;

public class UrlLog {
	public String url;
	public long size;
	double p;
	boolean isComplete;
	private OnProgressChangeListener listener;

	UrlLog(String urlText, long size) {
		this.url = urlText;
		this.size = size;
		this.isComplete = false;
	}

	public void setIsComplete(boolean o) {
		this.isComplete = o;
	}

	public void setProgress(double p) {
		this.p = p;
		listener.onChange(p);
	}

	public void setOnProgressChangeListener(OnProgressChangeListener listener) {
		this.listener = listener;
	}

	public interface OnProgressChangeListener {
		void onChange(double p);
	}
}