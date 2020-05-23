package com.kejunyao.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database行为接口
 *
 * @author kejunyao
 * @since 2018年09月14日
 */
public interface Action {

    void action(SQLiteDatabase db);

}
