package com.kejunyao.db;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.ParameterizedType;

/**
 * 数据库DAO操作基类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public abstract class AbstractDao<T> implements Dao<T> {

    private final Class<T> persistentClass;
    protected ContentResolver mContentResolver;
    protected SQLiteOpenHelper mSQLiteOpenHelper;
    protected String mProviderAuthority;

    @SuppressWarnings("unchecked")
    public AbstractDao() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

    }

    Class<T> getPersistentClass() {
        return persistentClass;
    }

    void setContentResolver(ContentResolver resolver) {
        this.mContentResolver = resolver;
    }

    void setProviderAuthority(String authority) {
        this.mProviderAuthority = authority;
    }

    void setSQLiteOpenHelper(SQLiteOpenHelper openHelper) {
        this.mSQLiteOpenHelper = openHelper;
    }
}
