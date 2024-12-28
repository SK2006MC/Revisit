package com.sk.revisit.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class SQLiteDBM {

    private static final String DATABASE_NAME = "revisit_web_db";
    private static final int DATABASE_VERSION = 1;

    // Table for stored urls
    private static final String TABLE_STORED_URLS = "stored_urls";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_HOST = "host";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_FILE_SIZE = "file_size";
    private static final String COLUMN_LAST_MODIFIED = "last_modified";
    private static final String COLUMN_ETAG = "etag";

    // Table for download requests
    private static final String TABLE_DOWNLOAD_REQUESTS = "download_requests";
    private static final String COLUMN_REQUEST_ID = "id";
    private static final String COLUMN_REQUEST_URL = "url";
    private static final String COLUMN_REQUEST_HOST = "host"; // For grouping requests

    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public SQLiteDBM(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // url Storage Operations
    public long storeUrl(Uri url, String filePath, long fileSize, String lastModified, String etag) {
        open();
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, url.toString());
        values.put(COLUMN_HOST, url.getHost());
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_FILE_SIZE, fileSize);
        values.put(COLUMN_LAST_MODIFIED, lastModified);
        values.put(COLUMN_ETAG, etag);
        long id = db.insert(TABLE_STORED_URLS, null, values);
        close();
        return id;
    }

    public Map<String, String> getStoredUrlDetails(String url) {
        open();
        Cursor cursor = db.query(TABLE_STORED_URLS,
                new String[]{COLUMN_FILE_PATH, COLUMN_LAST_MODIFIED, COLUMN_ETAG},
                COLUMN_URL + "=?",
                new String[]{url}, null, null, null);
        Map<String, String> details = null;
        if (cursor != null && cursor.moveToFirst()) {
            details = new HashMap<>();
            details.put("filePath", cursor.getString(0));
            details.put("lastModified", cursor.getString(1));
            details.put("etag", cursor.getString(2));
            cursor.close();
        }
        close();
        return details;
    }

    public long addDownloadRequest(Uri url) {
        open();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REQUEST_URL, url.toString());
        values.put(COLUMN_REQUEST_HOST, url.getHost());
        long id = db.insert(TABLE_DOWNLOAD_REQUESTS, null, values);
        close();
        return id;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_URLS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STORED_URLS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_URL + " TEXT UNIQUE,"
                    + COLUMN_FILE_PATH + " TEXT,"
                    + COLUMN_HOST + " TEXT,"
                    + COLUMN_FILE_SIZE + " INTEGER,"
                    + COLUMN_LAST_MODIFIED + " TEXT,"
                    + COLUMN_ETAG + " TEXT" + ")";
            db.execSQL(CREATE_URLS_TABLE);

            String CREATE_DOWNLOAD_REQUESTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DOWNLOAD_REQUESTS + "("
                    + COLUMN_REQUEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_REQUEST_URL + " TEXT UNIQUE,"
                    + COLUMN_REQUEST_HOST + " TEXT" + ")";
            db.execSQL(CREATE_DOWNLOAD_REQUESTS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORED_URLS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOAD_REQUESTS);
            onCreate(db);
        }
    }
}
