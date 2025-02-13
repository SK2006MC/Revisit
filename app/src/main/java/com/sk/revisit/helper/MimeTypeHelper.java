package com.sk.revisit.helper;

import android.net.Uri;

import com.sk.revisit.MyUtils;
import com.sk.revisit.log.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MimeTypeHelper {

	private static final String TAG = "MimeTypeHelper";
	private final OkHttpClient client;
	private final MyUtils utils;
	private static final String MIME_FILE_EXTENSION = ".mime";

	public MimeTypeHelper(MyUtils utils) {
		this.utils = utils;
		this.client = utils.client;
	}

	/**
	 * Creates MIME type metadata for a given URI.
	 *
	 * @param uri The URI of the resource.
	 */
	public void createMimeTypeMeta(Uri uri) {

		if (!MyUtils.isNetworkAvailable) {
			Log.w(TAG, "Network not available. Skipping MIME type metadata creation.");
			return;
		}

		String localPath = utils.buildLocalPath(uri);
		if (localPath == null) {
			Log.e(TAG, "Failed to build local path for URI: " + uri);
			return;
		}
		String mimeType = fetchMimeTypeFromNetwork(uri);
		if (mimeType != null) {
			createMimeTypeMetaFile(localPath, mimeType);
		}
	}

	/**
	 * Fetches the MIME type of a resource from the network.
	 *
	 * @param uri The URI of the resource.
	 * @return The MIME type as a String, or null if it cannot be determined.
	 */
	private String fetchMimeTypeFromNetwork(Uri uri) {
		try {
			Request request = new Request.Builder().head().url(uri.toString()).build();
			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					Log.e(TAG, "Failed to get MIME type. HTTP error code: " + response.code() + " for URI: " + uri);
					return null;
				}

				ResponseBody body = response.body();
				if (body == null) {
					Log.e(TAG, "Response body is null for URI: " + uri);
					return null;
				}

				MediaType mediaType = body.contentType();
				if (mediaType == null) {
					Log.e(TAG, "Content type is null for URI: " + uri);
					return null;
				}

				return mediaType.toString().split(";")[0]; // Extract only the main type/subtype
			}
		} catch (Exception e) {
			Log.e(TAG, "An unexpected error occurred while processing URI: " + uri, e);
			return null;
		}
	}

	/**
	 * Creates MIME type metadata file.
	 *
	 * @param localPath The local path.
	 * @param mimeType  The MIME type.
	 */
	public void createMimeTypeMetaFile(String localPath, String mimeType) {
		String filepath = localPath + MIME_FILE_EXTENSION;
		File file = new File(filepath);

		try {
			File parentDir = file.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				if (!parentDir.mkdirs()) {
					Log.e(TAG, "Failed to create parent directories for: " + filepath);
					return;
				}
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(mimeType);
			}
		} catch (IOException e) {
			Log.e(TAG, "Error creating MIME type metadata file for path: " + filepath, e);
		}
	}

	/**
	 * Gets the MIME type from the metadata file.
	 *
	 * @param filepath The file path.
	 * @return The MIME type, or null if not found.
	 */
	public String getMimeTypeFromMeta(String filepath) {
		filepath = filepath + MIME_FILE_EXTENSION;
		File file = new File(filepath);
		if (!file.exists()) {
			return getMimeTypeFromFileSystem(filepath);
		}

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
			return bufferedReader.readLine();
		} catch (IOException e) {
			Log.e(TAG, "Error reading MIME type from file: " + filepath, e);
			return getMimeTypeFromFileSystem(filepath);
		}
	}

	/**
	 * Gets the MIME type from the file system using java.nio.file.Files.probeContentType
	 *
	 * @param localPath The local path.
	 * @return The MIME type, or null if not found.
	 */
	public String getMimeTypeFromFileSystem(String localPath) {
		try {
			return Files.probeContentType(Paths.get(localPath));
		} catch (IOException e) {
			Log.e(TAG, "Error probing MIME type from file system: " + localPath, e);
			return null;
		}
	}
}