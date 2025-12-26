package com.sk.revisit.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.core.content.ContextCompat
import java.io.File

/**
 * StorageHelper
 *
 * Utilities for working with SAF tree URIs:
 * - Persisting takePersistableUriPermission
 * - Best-effort conversion from tree:// URIs to absolute filesystem paths (may return null)
 *
 * Notes:
 * - The conversion is heuristic and not guaranteed to work on all devices/Android versions.
 * - Prefer using DocumentFile.fromTreeUri(...) and ContentResolver streams for reliable file access.
 */
object StorageHelper {

    /**
     * Persist read/write permission for the provided tree URI.
     */
    fun takePersistableUriPermission(context: Context?, uri: Uri?) {
        if (context == null || uri == null) return
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (ignored: Exception) {
            // Best-effort: some OEMs/URIs may throw; ignoring to avoid crash
            ignored.printStackTrace()
        }
    }

    /**
     * Return a display path for the given tree URI. This will attempt to return an absolute
     * filesystem path when possible; otherwise it falls back to the URI string.
     *
     * @param context application context
     * @param treeUri the tree URI returned by OpenDocumentTree
     * @return absolute path string if resolvable, or uri.toString() fallback
     */
    fun getDisplayPath(context: Context?, treeUri: Uri?): String? {
        val full = getFullPathFromTreeUri(context, treeUri)
        return full ?: treeUri?.toString()
    }

    /**
     * Attempt to resolve a SAF tree URI to a filesystem path. Best-effort heuristic:
     * - For "primary" volume returns Environment.getExternalStorageDirectory() +/- relative path.
     * - For removable volumes tries to infer the mount point from Context.getExternalFilesDirs().
     * Returns null if it cannot be safely determined.
     */
    fun getFullPathFromTreeUri(context: Context?, treeUri: Uri?): String? {
        if (context == null || treeUri == null) return null
        try {
            val docId = DocumentsContract.getTreeDocumentId(treeUri) ?: return null

            val split = docId.split(":").toTypedArray()
            val type = if (split.isNotEmpty()) split[0] else ""
            val relativePath = if (split.size > 1) split[1] else ""

            if ("primary".equals(type, ignoreCase = true)) {
                val external = Environment.getExternalStorageDirectory()
                return if (relativePath.isEmpty()) {
                    external.absolutePath
                } else {
                    external.absolutePath + "/" + relativePath
                }
            } else {
                val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
                for (file in externalDirs) {
                    if (file == null) continue
                    val absolutePath = file.absolutePath
                    val androidDataIndex = absolutePath.indexOf("/Android/")
                    if (androidDataIndex >= 0) {
                        val candidateRoot = absolutePath.substring(0, androidDataIndex)
                        if (candidateRoot.lowercase().contains(type.lowercase()) || candidateRoot.endsWith(type)) {
                            return if (relativePath.isEmpty()) {
                                candidateRoot
                            } else {
                                candidateRoot + "/" + relativePath
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
