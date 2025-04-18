package com.sk.revisit.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.fragments.SettingsFragment;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_PICK_FOLDER = 101;
	MySettingsManager settingsManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
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
				assert path != null;
				String root = path.split(":")[1];
				String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + root;
				settingsManager.setRootStoragePath(folderPath);
			}
		}
	}
}