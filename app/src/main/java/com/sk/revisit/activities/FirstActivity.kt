package com.sk.revisit.activities

import android.Manifest
import android.content.Intent
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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private val openDirectoryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                // Persist permission for this URI so we can access it later
                StorageHelper.takePersistableUriPermission(this, uri)

                // Get a friendly display path (best-effort absolute path or fallback to URI string)
                val displayPath = StorageHelper.getDisplayPath(this, uri)

                // Save and show
                settingsManager.rootStoragePath = displayPath
                binding.rootPathTextView.text = displayPath

                // If you need to enumerate files use DocumentFile.fromTreeUri(this, uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsManager = getRevisitApp().mySettingsManager

        binding.pickPath.setOnClickListener { openDirectoryChooser() }
        if (!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
            !PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

        binding.start.setOnClickListener {
            settingsManager.isFirst = false
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun openDirectoryChooser() {
        openDirectoryLauncher.launch(null)
    }
}
