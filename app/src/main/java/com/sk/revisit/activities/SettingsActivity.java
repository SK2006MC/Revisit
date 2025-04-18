package com.sk.revisit.activities;

import android.os.Bundle;

import com.sk.revisit.fragments.SettingsFragment;

public class SettingsActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }
}