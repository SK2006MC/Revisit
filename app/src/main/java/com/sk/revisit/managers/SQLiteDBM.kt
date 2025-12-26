package com.sk.revisit.managers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log
import okhttp3.Headers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class SQLiteDBM(context: Context, databasePath: String) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context, databasePath)
    private val executor: ExecutorService = Executors.newFixedThreadPool(2)
    private val lock = ReentrantReadWriteLock()

    /**
     * Inserts a URL if it does not already exist.
     */
    fun insertIntoUrlsIfNotExists(url: Uri, filePath: String, fileSize: Long, headers: Headers) {
        executor.execute {
            lock.write {
                dbHelper.writableDatabase.use { db ->
                    val values = ContentValues().apply {
                        put(COLUMN_URL, url.toString())
                        put(COLUMN_HOST, url.host)
                        put(COLUMN_FILE_PATH, filePath)
                        put(COLUMN_FILE_SIZE, fileSize)
                        put(COLUMN_HEADERS, headers.toString())
                    }

                    val id = db.insertWithOnConflict(TABLE_STORED_URLS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
                    Log.d(TAG, if (id == -1L) "Failed to insert URL: $url" else "Inserted URL: $url with ID: $id")
                }
            }
        }
    }

    /**
     * Retrieves stored URL details.
     */
    fun selectAllFromUrlsWhereUrl(url: String): Map<String, String>? {
        lock.read {
            dbHelper.readableDatabase.use { db ->
                db.query(
                    TABLE_STORED_URLS, arrayOf(COLUMN_FILE_PATH, COLUMN_LAST_MODIFIED, COLUMN_ETAG),
                    "$COLUMN_URL=?", arrayOf(url), null, null, null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        return mapOf(
                            "filePath" to cursor.getString(0),
                            "lastModified" to (cursor.getString(1) ?: ""),
                            "etag" to (cursor.getString(2) ?: "")
                        )
                    }
                }
            }
        }
        return null
    }

    /**
     * Inserts a new download request.
     */
    fun insertIntoQueIfNotExists(url: Uri) {
        executor.execute {
            lock.write {
                dbHelper.writableDatabase.use { db ->
                    val values = ContentValues().apply {
                        put(COLUMN_REQUEST_URL, url.toString())
                        put(COLUMN_REQUEST_HOST, url.host)
                    }
                    db.insertWithOnConflict(TABLE_DOWNLOAD_REQUESTS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
        }
    }

    /**
     * Retrieves unique hosts from stored URLs.
     */
    fun selectUniqueHostFromUrls(): Set<String> {
        lock.read {
            dbHelper.readableDatabase.use { db ->
                db.query(true, TABLE_STORED_URLS, arrayOf(COLUMN_HOST), null, null, null, null, null, null).use { cursor ->
                    val hosts = mutableSetOf<String>()
                    while (cursor.moveToNext()) {
                        hosts.add(cursor.getString(0))
                    }
                    return hosts
                }
            }
        }
    }

    /**
     * Retrieves a list of stored URLs.
     */
    fun selectUrlFromUrls(): ArrayList<String> {
        lock.read {
            dbHelper.readableDatabase.use { db ->
                db.query(TABLE_STORED_URLS, arrayOf(COLUMN_URL), null, null, null, null, null).use { cursor ->
                    val urls = ArrayList<String>()
                    while (cursor.moveToNext()) {
                        urls.add(cursor.getString(0))
                    }
                    return urls
                }
            }
        }
    }

    /**
     * Database helper class.
     */
    private class DatabaseHelper(context: Context, val databasePath: String) :
        SQLiteOpenHelper(context, databasePath, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS $TABLE_STORED_URLS (" +
                        "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COLUMN_URL TEXT UNIQUE, " +
                        "$COLUMN_HOST TEXT, " +
                        "$COLUMN_FILE_PATH TEXT, " +
                        "$COLUMN_FILE_SIZE INTEGER, " +
                        "$COLUMN_LAST_MODIFIED TEXT, " +
                        "$COLUMN_HEADERS TEXT, " +
                        "$COLUMN_ETAG TEXT" +
                        ")"
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS $TABLE_DOWNLOAD_REQUESTS (" +
                        "$COLUMN_REQUEST_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COLUMN_REQUEST_URL TEXT UNIQUE, " +
                        "$COLUMN_REQUEST_HOST TEXT" +
                        ")"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_STORED_URLS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_DOWNLOAD_REQUESTS")
            onCreate(db)
        }
    }

    companion object {
        private val TAG = SQLiteDBM::class.java.simpleName

        private const val DATABASE_VERSION = 2

        private const val TABLE_STORED_URLS = "stored_urls"
        private const val COLUMN_ID = "id"
        private const val COLUMN_URL = "url"
        private const val COLUMN_HOST = "host"
        private const val COLUMN_FILE_PATH = "file_path"
        private const val COLUMN_FILE_SIZE = "file_size"
        private const val COLUMN_LAST_MODIFIED = "last_modified"
        private const val COLUMN_HEADERS = "headers"
        private const val COLUMN_ETAG = "etag"

        private const val TABLE_DOWNLOAD_REQUESTS = "download_requests"
        private const val COLUMN_REQUEST_ID = "id"
        private const val COLUMN_REQUEST_URL = "url"
        private const val COLUMN_REQUEST_HOST = "host"
    }
}
