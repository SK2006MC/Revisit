package com.sk.revisit.components;

import android.content.Context;
import android.widget.Toast;

import com.sk.revisit.activities.BaseActivity;

public class Component {
    protected String TAG;
    protected Context context;
    protected BaseActivity activity;

    Component(BaseActivity activity) {
        this.activity = activity;
        this.context = activity;
        this.TAG = this.getClass().getSimpleName();
    }

    public void alert(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
