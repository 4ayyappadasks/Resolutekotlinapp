package com.example.mykotlinapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.ContentUris
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import com.example.mykotlinapp.database.DatabaseHelper

class UserContentProvider : ContentProvider() {

    private lateinit var dbHelper: DatabaseHelper

    companion object {
        const val AUTHORITY = "com.example.mykotlinapp.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/users")

        private const val USERS = 1
        private const val USER_ID = 2

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "users", USERS)
            addURI(AUTHORITY, "users/#", USER_ID)
        }
    }

    override fun onCreate(): Boolean {
        context?.let {
            dbHelper = DatabaseHelper(it)
        } ?: return false
        return true
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase

        // Ensure projection includes _id
        val modifiedProjection = projection?.map {
            if (it == "id") "_id" else it
        }?.toTypedArray() ?: arrayOf("_id", "name", "email")

        val cursor = when (uriMatcher.match(uri)) {
            USERS -> db.query(DatabaseHelper.TABLE_USERS, modifiedProjection, selection, selectionArgs, null, null, sortOrder)
            USER_ID -> {
                val id = uri.lastPathSegment ?: throw IllegalArgumentException("Invalid user ID")
                db.query(DatabaseHelper.TABLE_USERS, modifiedProjection, "_id=?", arrayOf(id), null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }


    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        if (id == -1L) throw SQLException("Failed to insert row into $uri")
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val rowsUpdated = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs)
        if (rowsUpdated > 0) context?.contentResolver?.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(DatabaseHelper.TABLE_USERS, selection, selectionArgs)
        if (rowsDeleted > 0) context?.contentResolver?.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        USERS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.users"
        USER_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.users"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }
}
