package com.sk.revisit.activities;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.sk.revisit.MyUtils;
import com.sk.revisit.databinding.ActivityUtilsBinding;
import com.sk.revisit.managers.MySettingsManager;

public class UtilsActivity extends BaseActivity {

	ActivityUtilsBinding binding;
	MyUtils utils;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityUtilsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		MySettingsManager sm = new MySettingsManager(this);
		utils = new MyUtils(this, sm.getRootStoragePath());

		binding.exeBuildLocalPath.setOnClickListener(v -> {
			try {
				String url = binding.url.getText().toString();
				String localPath = utils.buildLocalPath(Uri.parse(url));
				binding.localpath.setText(localPath);
			} catch (Exception e) {
				alert(e.toString());
			}
		});
	}
}
