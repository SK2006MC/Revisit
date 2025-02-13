package com.sk.revisit.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sk.revisit.R;
import com.sk.revisit.databinding.ActivityFirstBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.File;

public class FirstActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_PICK_FOLDER = 101;
	private static final int PERMISSION_REQUEST_STORAGE = 102;
	ActivityFirstBinding binding;
	MySettingsManager settingsManager;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityFirstBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);

		binding.pickPath.setOnClickListener((view) -> openDirectoryChooser());

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
		}

		binding.start.setOnClickListener((view) -> {
			settingsManager.setIsFirst(false);
			startActivity(new Intent(this, MainActivity.class));
			finish();
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permission, grantResults);
		if (requestCode == PERMISSION_REQUEST_STORAGE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
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
