package com.sk.revisit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.databinding.ActivityUpdateBinding;

public class UpdateActivity extends AppCompatActivity {

	ActivityUpdateBinding binding;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		binding = ActivityUpdateBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
	}

}