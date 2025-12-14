package com.sk.revisit.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.core.content.ContextCompat;

import java.io.File;

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
public final class StorageHelper {

    private StorageHelper() { /* no-op */ }

    /**
     * Persist read/write permission for the provided tree URI.
     */
    public static void takePersistableUriPermission(Context context, Uri uri) {
        if (context == null || uri == null) return;
        try {
            context.getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Exception ignored) {
            // Best-effort: some OEMs/URIs may throw; ignoring to avoid crash
            ignored.printStackTrace();
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
    public static String getDisplayPath(Context context, Uri treeUri) {
        String full = getFullPathFromTreeUri(context, treeUri);
        return full != null ? full : (treeUri != null ? treeUri.toString() : null);
    }

    /**
     * Attempt to resolve a SAF tree URI to a filesystem path. Best-effort heuristic:
     * - For "primary" volume returns Environment.getExternalStorageDirectory() +/- relative path.
     * - For removable volumes tries to infer the mount point from Context.getExternalFilesDirs().
     * Returns null if it cannot be safely determined.
     */
    public static String getFullPathFromTreeUri(final Context context, final Uri treeUri) {
        if (context == null || treeUri == null) return null;
        try {
            final String docId = DocumentsContract.getTreeDocumentId(treeUri);
            if (docId == null) return null;

            String[] split = docId.split(":");
            String type = split.length > 0 ? split[0] : "";
            String relativePath = split.length > 1 ? split[1] : "";

            if ("primary".equalsIgnoreCase(type)) {
                File external = Environment.getExternalStorageDirectory();
                if (relativePath == null || relativePath.isEmpty()) {
                    return external.getAbsolutePath();
                } else {
                    return external.getAbsolutePath() + "/" + relativePath;
                }
            } else {
                File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);
                for (File file : externalDirs) {
                    if (file == null) continue;
                    String absolutePath = file.getAbsolutePath();
                    int androidDataIndex = absolutePath.indexOf("/Android/");
                    if (androidDataIndex >= 0) {
                        String candidateRoot = absolutePath.substring(0, androidDataIndex);
                        if (candidateRoot.toLowerCase().contains(type.toLowerCase()) || candidateRoot.endsWith(type)) {
                            if (relativePath == null || relativePath.isEmpty()) {
                                return candidateRoot;
                            } else {
                                return candidateRoot + "/" + relativePath;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}