package com.sk.revisit.activities;

import android.os.Bundle;

import com.sk.revisit.databinding.ActivityAboutBinding;

public class AboutActivity extends BaseActivity {

    ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}