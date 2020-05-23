package com.kejunyao.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 简单实现的{@link SQLiteOpenHelper}
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public class SimpleSQLiteOpenHelper extends SQLiteOpenHelper {

    public SimpleSQLiteOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SimpleSQLiteDBManager.getInstance().createAllTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SimpleSQLiteDBManager.getInstance().onUpgrade(db, oldVersion, newVersion);
    }
}
