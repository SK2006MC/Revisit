package com.sk.revisit.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.R;
import com.sk.revisit.databinding.ActivitySettingsBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_PICK_FOLDER = 101;
	ActivitySettingsBinding binding;
	MySettingsManager settingsManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivitySettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);

		binding.rootPathTextView.setText(settingsManager.getRootStoragePath());

		binding.pickPath.setOnClickListener((view) -> {
			openDirectoryChooser();
		});
	}

	void initUI() {
		binding.reqFileName.setText(settingsManager.getReqFileName());
	}

	private void openDirectoryChooser() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == RESULT_OK) {
			if (data != null && data.getData() != null) {
				Uri uri = data.getData();
				String path = uri.getPath();
				String root = path.split(":")[1];
				String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + root;
				if (folderPath != null) {
					settingsManager.setRootStoragePath(folderPath);
					binding.rootPathTextView.setText(settingsManager.getRootStoragePath());
				} else {
					Toast.makeText(this, "Cannot choose this directory", Toast.LENGTH_LONG).show();
					binding.rootPathTextView.setText(R.string.none);
				}
			}
		}
	}
}