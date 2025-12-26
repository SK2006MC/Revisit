package com.sk.revisit.activities

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.sk.revisit.databinding.ActivityFirstBinding
import com.sk.revisit.helper.PermissionHelper
import com.sk.revisit.helper.StorageHelper
import com.sk.revisit.managers.MySettingsManager

class FirstActivity : BaseActivity() {

    private lateinit var binding: ActivityFirstBinding
    private lateinit var settingsManager: MySettingsManager

    // Launcher for Storage Permissions
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            val writeGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

            if (readGranted && writeGranted) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                alert("Permission Denied. The app cannot function without storage access.")
                finish()
            }
        }

    // Launcher for Directory Selection (SAF)
    private val openDirectoryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                // Persist permission for this URI
                StorageHelper.takePersistableUriPermission(this, it)

                // Get a friendly display path
                StorageHelper.getDisplayPath(this, it)?.let { displayPath ->
                    settingsManager.rootStoragePath = displayPath
                    binding.rootPathTextView.text = displayPath
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Using the property inherited from BaseActivity
        settingsManager = revisitApp.mySettingsManager!!

        setupListeners()
        checkAndRequestPermissions()
    }

    private fun setupListeners() {
        with(binding) {
            pickPath.setOnClickListener {
                openDirectoryLauncher.launch(null)
            }

            start.setOnClickListener {
                if (settingsManager.rootStoragePath.isEmpty()) {
                    alert("Please select a storage path first")
                } else {
                    settingsManager.isFirst = false
                    // Using the generic helper from BaseActivity
                    startMyActivity<MainActivity>(fini = true)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val hasRead = PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val hasWrite = PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (!hasRead || !hasWrite) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }
}