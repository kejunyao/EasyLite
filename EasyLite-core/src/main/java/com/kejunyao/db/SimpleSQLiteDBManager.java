package com.kejunyao.db;

import android.content.Context;

/**
 * 简单通用数据库操作Manager
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public class SimpleSQLiteDBManager extends DatabaseController {

    private static volatile SimpleSQLiteDBManager sInstance;

    private SimpleSQLiteDBManager(Context context, String name, int version) {
        super(Utils.newSingleFixedThreadPool(), new SimpleSQLiteOpenHelper(context, name, version));
    }

    public static SimpleSQLiteDBManager init(Context context, String name, int version) {
        if (sInstance == null) {
            synchronized (SimpleSQLiteDBManager.class) {
                if (sInstance == null) {
                    sInstance = new SimpleSQLiteDBManager(context, name, version);
                }
            }
        }
        return sInstance;
    }

    public static SimpleSQLiteDBManager getInstance() {
        synchronized (SimpleSQLiteDBManager.class) {
            Assert.notNull(sInstance, "Please call init method first!");
            return sInstance;
        }
    }

    public SimpleSQLiteDBManager addDao(SimpleSQLiteDaoImpl dao) {
        super.addDao(dao);
        return this;
    }

    public DatabaseController addDao(AbstractDao dao) {
        return this;
    }
}
