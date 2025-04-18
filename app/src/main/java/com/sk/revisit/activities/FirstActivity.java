package com.sk.revisit.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.sk.revisit.databinding.ActivityFirstBinding;
import com.sk.revisit.helper.PermissionHelper;
import com.sk.revisit.managers.MySettingsManager;

public class FirstActivity extends BaseActivity {

    ActivityFirstBinding binding;
    MySettingsManager settingsManager;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (Boolean.TRUE.equals(permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE)) &&
                        Boolean.TRUE.equals(permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    private final ActivityResultLauncher<Uri> openDirectoryLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    settingsManager.setRootStoragePath(uri.toString());
                    binding.rootPathTextView.setText(settingsManager.getRootStoragePath());
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirstBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = getRevisitApp().getMySettingsManager();

        binding.pickPath.setOnClickListener((view) -> openDirectoryChooser());
        if (!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                !PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }

        binding.start.setOnClickListener((view) -> {
            settingsManager.setIsFirst(false);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void openDirectoryChooser() {
        openDirectoryLauncher.launch(null);
    }
}
