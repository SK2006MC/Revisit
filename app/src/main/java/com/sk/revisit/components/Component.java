package com.sk.revisit.components;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Component {
	protected String TAG;
	protected Context context;
	protected AppCompatActivity activity;

	Component(AppCompatActivity activity) {
		this.activity = activity;
		this.context = activity;
		this.TAG = this.getClass().getSimpleName();
	}

	public void alert(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
