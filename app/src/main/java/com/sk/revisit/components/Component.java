package com.sk.revisit.components;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Component {
	public String TAG;
	public Context context;
	public AppCompatActivity activity;

	Component(AppCompatActivity activity) {
		this.activity = activity;
		this.context = activity;
		this.TAG = this.getClass().getSimpleName();
	}

	public void alert(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
