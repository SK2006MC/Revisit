package com.sk.revisit.activities;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.Revisit;

public class BaseActivity extends AppCompatActivity {
    public String TAG = this.getClass().getSimpleName();

    public void alert(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void startMyActivity(Class<?> activity) {
        startMyActivity(activity, false);
    }

    public void startMyActivity(Class<?> activityClass, boolean fini) {
        MainActivity.bpn = 0;
        startActivity(new Intent(this, activityClass));
        if (fini) finish();
    }

    public Revisit getRevisitApp() {
        return (Revisit) getApplication();
    }

}
