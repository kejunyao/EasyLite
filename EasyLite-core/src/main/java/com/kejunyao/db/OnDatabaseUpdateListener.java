package com.kejunyao.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库升级Listener
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public interface OnDatabaseUpdateListener {

    void onDatabaseUpdate(SQLiteDatabase db, int currVersion);

}
