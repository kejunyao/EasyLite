package com.kejunyao.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.kejunyao.log.Log;

/**
 * ContentProvider基类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public abstract class AbstractContentProvider extends ContentProvider {

    private String mUriPrefix;
    private SQLiteOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = getSQLiteOpenHelper();
        mUriPrefix = getUriPrefix();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            String table = getTable(uri);
            if (Log.isLogEnabled()) {
                Log.dWithNoSwitch(getClass().getSimpleName(), "toEntity, uri: ", ((uri == null) ? "null" : uri.toString()), ", table: ", table);
            }
            if (TextUtils.isEmpty(table)) {
                throw new IllegalArgumentException("Unknown URL " + uri);
            }
            SQLiteDatabase db = mHelper.getReadableDatabase();
            cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        } catch (Exception e) {
            if (Log.isLogEnabled()) {
                Log.e(getClass().getSimpleName(), "toEntity failed - ", e);
            }
        } finally {
            return cursor;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        try {
            String table = getTable(uri);
            if (Log.isLogEnabled()) {
                Log.dWithNoSwitch(getClass().getSimpleName(), "insert, uri: ", ((uri == null) ? "null" : uri.toString()), ", table: ", table);
            }
            if (TextUtils.isEmpty(table)) {
                throw new IllegalArgumentException("Unknown URL " + uri);
            }
            SQLiteDatabase db = mHelper.getWritableDatabase();
            long rowId = db.insert(table, null, values);
            if (rowId > 0) {
                result = ContentUris.appendId(tableUri(table).buildUpon(), rowId).build();
            }
        } catch (Exception e) {
            if (Log.isLogEnabled()) {
                Log.e(getClass().getSimpleName(), "insert failed - ", e);
            }
        } finally {
            return result;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = -1;
        try {
            String table = getTable(uri);
            if (Log.isLogEnabled()) {
                Log.dWithNoSwitch(getClass().getSimpleName(), "delete, uri: " + ((uri == null) ? "null" : uri.toString()) + ", table: " + table);
            }
            if (TextUtils.isEmpty(table)) {
                throw new IllegalArgumentException("Unknown URL " + uri);
            }
            SQLiteDatabase db = mHelper.getWritableDatabase();
            result = db.delete(table, selection, selectionArgs);
        } catch (Exception e) {
            if (Log.isLogEnabled()) {
                Log.e(getClass().getSimpleName(), "delete failed - ", e);
            }
        } finally {
            return result;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int result = -1;
        try {
            String table = getTable(uri);
            if (Log.isLogEnabled()) {
                Log.dWithNoSwitch(getClass().getSimpleName(), "update, uri: ", ((uri == null) ? "null" : uri.toString()), ", table: ", table);
            }
            if (TextUtils.isEmpty(table)) {
                throw new IllegalArgumentException("Unknown URL " + uri);
            }
            SQLiteDatabase db = mHelper.getWritableDatabase();
            result = db.update(table, values, selection, selectionArgs);
        } catch (Exception e) {
            if (Log.isLogEnabled()) {
                Log.e(getClass().getSimpleName(), "update failed - ", e);
            }
        } finally {
            return result;
        }
    }

    public abstract SQLiteOpenHelper getSQLiteOpenHelper();

    public abstract String getUriPrefix();

    private Uri tableUri(String table) {
        return Uri.parse(mUriPrefix + table);
    }

    private String getTable(Uri uri) {
        String uriStr = uri.toString();
        if (TextUtils.isEmpty(uriStr)) {
            return null;
        }
        int prefixLength = mUriPrefix.length();
        if (uriStr.length() <= prefixLength) {
            return null;
        }
        String result = uriStr.substring(prefixLength);
        int index = result.indexOf('/');
        if (index > 0) {
            return result.substring(0, index);
        }
        return result;
    }
}
