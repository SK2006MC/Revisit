package com.sk.revisit.helper

import android.net.Uri
import android.util.Log
import com.sk.revisit.MyUtils
import com.sk.revisit.Revisit
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths

class MimeHelper(private val utils: MyUtils) {

    private val client: OkHttpClient = utils.client

    /**
     * Creates MIME type metadata for a given URI.
     *
     * @param uri The URI of the resource.
     */
    fun createMimeTypeMeta(uri: Uri) {
        if (!Revisit.isNetworkAvailable) {
            Log.w(TAG, "Network not available. Skipping MIME type metadata creation.")
            return
        }

        val localPath = utils.buildLocalPath(uri)
        if (localPath == null) {
            Log.e(TAG, "Failed to build local path for URI: $uri")
            return
        }
        val mimeType = fetchMimeTypeFromNetwork(uri)
        if (mimeType != null) {
            createMimeTypeMetaFile(localPath, mimeType)
        }
    }

    /**
     * Fetches the MIME type of a resource from the network.
     *
     * @param uri The URI of the resource.
     * @return The MIME type as a String, or null if it cannot be determined.
     */
    private fun fetchMimeTypeFromNetwork(uri: Uri): String? {
        return try {
            val request = Request.Builder().head().url(uri.toString()).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to get MIME type. HTTP error code: ${response.code} for URI: $uri")
                    return null
                }

                val body = response.body
                if (body == null) {
                    Log.e(TAG, "Response body is null for URI: $uri")
                    return null
                }

                val mediaType = body.contentType()
                if (mediaType == null) {
                    Log.e(TAG, "Content type is null for URI: $uri")
                    return null
                }

                mediaType.toString().split(";")[0] // Extract only the main type/subtype
            }
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred while processing URI: $uri", e)
            null
        }
    }

    /**
     * Creates MIME type metadata file.
     *
     * @param localPath The local path.
     * @param mimeType  The MIME type.
     */
    fun createMimeTypeMetaFile(localPath: String, mimeType: String) {
        val filepath = localPath + MIME_FILE_EXTENSION
        val file = File(filepath)

        try {
            val parentDir = file.parentFile
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    Log.e(TAG, "Failed to create parent directories for: $filepath")
                    return
                }
            }

            BufferedWriter(FileWriter(file)).use { writer ->
                writer.write(mimeType)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creating MIME type metadata file for path: $filepath", e)
        }
    }

    /**
     * Gets the MIME type from the metadata file.
     *
     * @param filepath The file path.
     * @return The MIME type, or null if not found.
     */
    fun getMimeTypeFromMeta(filepath: String): String? {
        val metaFilepath = filepath + MIME_FILE_EXTENSION
        val file = File(metaFilepath)
        if (!file.exists()) {
            return mimeFromProbeCont(filepath)
        }

        return try {
            BufferedReader(FileReader(file)).use { bufferedReader ->
                bufferedReader.readLine().split(";")[0]
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading MIME type from file: $metaFilepath", e)
            mimeFromProbeCont(filepath)
        }
    }

    /**
     * Gets the MIME type from the file system using java.nio.file.Files.probeContentType
     *
     * @param localPath The local path.
     * @return The MIME type, or null if not found.
     */
    fun mimeFromProbeCont(localPath: String): String? {
        return try {
            Files.probeContentType(Paths.get(localPath))
        } catch (e: IOException) {
            Log.e(TAG, "Error probing MIME type from file system: $localPath", e)
            null
        }
    }

    companion object {
        private const val TAG = "MimeHelper"
        private const val MIME_FILE_EXTENSION = ".mime"
    }
}
