package com.sk.revisit.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.sk.revisit.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import okhttp3.Headers;

public class SQLiteDBM {
	private static final String TAG = SQLiteDBM.class.getSimpleName();

	private static final String DATABASE_NAME = "revisit_web_db";
	private static final int DATABASE_VERSION = 2;

	private static final String TABLE_STORED_URLS = "stored_urls";
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_URL = "url";
	private static final String COLUMN_HOST = "host";
	private static final String COLUMN_FILE_PATH = "file_path";
	private static final String COLUMN_FILE_SIZE = "file_size";
	private static final String COLUMN_LAST_MODIFIED = "last_modified";
	private static final String COLUMN_HEADERS = "headers";
	private static final String COLUMN_ETAG = "etag";

	private static final String TABLE_DOWNLOAD_REQUESTS = "download_requests";
	private static final String COLUMN_REQUEST_ID = "id";
	private static final String COLUMN_REQUEST_URL = "url";
	private static final String COLUMN_REQUEST_HOST = "host";

	private final DatabaseHelper dbHelper;
	private final ExecutorService executor;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public SQLiteDBM(Context context, String databasePath) {
		this.dbHelper = new DatabaseHelper(context, databasePath);
		this.executor = Executors.newFixedThreadPool(2);
	}

	/**
	 * Inserts a URL if it does not already exist.
	 */
	public void insertIntoUrlsIfNotExists(@NonNull Uri url, String filePath, long fileSize, Headers headers) {
		executor.execute(() -> {
			lock.writeLock().lock();
			try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
				ContentValues values = new ContentValues();
				values.put(COLUMN_URL, url.toString());
				values.put(COLUMN_HOST, url.getHost());
				values.put(COLUMN_FILE_PATH, filePath);
				values.put(COLUMN_FILE_SIZE, fileSize);
				values.put(COLUMN_HEADERS, headers.toString());

				long id = db.insertWithOnConflict(TABLE_STORED_URLS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
				Log.d(TAG, id == -1 ? "Failed to insert URL: " + url : "Inserted URL: " + url + " with ID: " + id);
			} finally {
				lock.writeLock().unlock();
			}
		});
	}

	/**
	 * Retrieves stored URL details.
	 */
	public Map<String, String> selectAllFromUrlsWhereUrl(String url) {
		lock.readLock().lock();
		try (SQLiteDatabase db = dbHelper.getReadableDatabase();
			 Cursor cursor = db.query(TABLE_STORED_URLS, new String[]{COLUMN_FILE_PATH, COLUMN_LAST_MODIFIED, COLUMN_ETAG},
					 COLUMN_URL + "=?", new String[]{url}, null, null, null)) {

			if (cursor.moveToFirst()) {
				Map<String, String> details = new HashMap<>();
				details.put("filePath", cursor.getString(0));
				details.put("lastModified", cursor.getString(1));
				details.put("etag", cursor.getString(2));
				return details;
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Inserts a new download request.
	 */
	public void insertIntoQueIfNotExists(@NonNull Uri url) {
		executor.execute(() -> {
			lock.writeLock().lock();
			try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
				ContentValues values = new ContentValues();
				values.put(COLUMN_REQUEST_URL, url.toString());
				values.put(COLUMN_REQUEST_HOST, url.getHost());
				db.insertWithOnConflict(TABLE_DOWNLOAD_REQUESTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			} finally {
				lock.writeLock().unlock();
			}
		});
	}

	/**
	 * Retrieves unique hosts from stored URLs.
	 */
	public Set<String> selectUniqueHostFromUrls() {
		lock.readLock().lock();
		try (SQLiteDatabase db = dbHelper.getReadableDatabase();
			 Cursor cursor = db.query(true, TABLE_STORED_URLS, new String[]{COLUMN_HOST}, null, null, null, null, null, null)) {

			Set<String> hosts = new HashSet<>();
			while (cursor.moveToNext()) {
				hosts.add(cursor.getString(0));
			}
			return hosts;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Retrieves a list of stored URLs.
	 */
	public ArrayList<String> selectUrlFromUrls() {
		lock.readLock().lock();
		try (SQLiteDatabase db = dbHelper.getReadableDatabase();
			 Cursor cursor = db.query(TABLE_STORED_URLS, new String[]{COLUMN_URL}, null, null, null, null, null)) {

			ArrayList<String> urls = new ArrayList<>();
			while (cursor.moveToNext()) {
				urls.add(cursor.getString(0));
			}
			return urls;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Database helper class.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		private final String databasePath;

		DatabaseHelper(Context context, String databasePath) {
			super(context, databasePath, null, DATABASE_VERSION);
			this.databasePath = databasePath;
		}

		@Override
		public void onCreate(@NonNull SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_STORED_URLS + " (" +
					COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					COLUMN_URL + " TEXT UNIQUE, " +
					COLUMN_HOST + " TEXT, " +
					COLUMN_FILE_PATH + " TEXT, " +
					COLUMN_FILE_SIZE + " INTEGER, " +
					COLUMN_LAST_MODIFIED + " TEXT, " +
					COLUMN_HEADERS + " TEXT, " +
					COLUMN_ETAG + " TEXT" +
					")");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DOWNLOAD_REQUESTS + " (" +
					COLUMN_REQUEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					COLUMN_REQUEST_URL + " TEXT UNIQUE, " +
					COLUMN_REQUEST_HOST + " TEXT" +
					")");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORED_URLS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOAD_REQUESTS);
			onCreate(db);
		}
	}
}