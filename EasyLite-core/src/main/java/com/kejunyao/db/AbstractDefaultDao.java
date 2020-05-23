package com.kejunyao.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库通用操作基类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public abstract class AbstractDefaultDao<T> extends AbstractDao<T> {

    protected final String PRIMARY_KEY_WHERE_CLAUSE;
    protected final boolean isPrimaryKeyAutoIncrement;
    protected final String[] mColumns;
    protected final String mPrimaryKey;

    public AbstractDefaultDao() {
        Column[] columns = getColumns();
        mColumns = new String[columns.length];
        boolean isAuto = false;
        String primaryKey = null;
        for (int i = 0, size = columns.length; i < size; i++) {
            Column column = columns[i];
            mColumns[i] = column.name();
            if (primaryKey == null) {
                if (column.isPrimaryKeyAuto()) {
                    primaryKey = column.name();
                    isAuto = true;
                } else if (column.isPrimaryKey()) {
                    primaryKey = column.name();
                }
            }
        }
        mPrimaryKey = primaryKey;
        PRIMARY_KEY_WHERE_CLAUSE = Utils.concat(primaryKey, " = ? ");
        isPrimaryKeyAutoIncrement = isAuto;
    }

    public abstract String getTableName();

    protected abstract Cursor query(String tableName, String[] columns, String whereClause, String[] whereArgs);

    public abstract Column[] getColumns();

    protected String[] primaryKeyWhereArgs(String primaryKey) {
        return new String[] {primaryKey};
    }

    public abstract T toEntity(Cursor c);

    public abstract ContentValues toContentValues(T entity);

    protected abstract boolean has(String log, String whereClause, String[] whereArgs);

    protected abstract boolean insert(String log, ContentValues values);

    protected abstract T query(String log, String whereClause, String[] whereArgs);

    protected abstract List<T> queryMany(String log, String whereClause, String[] whereArgs);

    protected abstract boolean update(String log, ContentValues values, String whereClause, String[] whereArgs);

    protected abstract boolean delete(String log, String whereClause, String[] whereArgs);

    public void createTable(SQLiteDatabase db) {
        if (Utils.isDebug()) {
            Utils.log(this, "create table ", getTableName(), " before");
        }
        db.execSQL(Utils.buildCreateTableSql(getTableName(), getColumns()));
        if (Utils.isDebug()) {
            Utils.log(this, "create table ", getTableName(), " done.");
        }
    }

    final String entityLog(String log) {
        return String.format("%1$s: %2$s ", log, getPersistentClass().getCanonicalName());
    }

    @Override
    public boolean has(String primaryKey) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean has(String primaryKey)";
        }
        return has(log, PRIMARY_KEY_WHERE_CLAUSE, primaryKeyWhereArgs(primaryKey));
    }

    @Override
    public boolean has(String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean has(String whereClause, String[] whereArgs)";
        }
        return has(log, whereClause, whereArgs);
    }

    protected List<T> queryMany(Cursor c) {
        int count = Utils.getCount(c);
        if (count > 0) {
            List<T> ts = new ArrayList<>(count);
            while (c.moveToNext()) {
                T entity = toEntity(c);
                if (entity != null) {
                    ts.add(entity);
                }
            }
            return ts;
        }
        return null;
    }

    @Override
    public T query(String primaryKey) {
        String log = null;
        if (Utils.isDebug()) {
            log = "T query(String primaryKey)";
        }
        return query(log, PRIMARY_KEY_WHERE_CLAUSE, primaryKeyWhereArgs(primaryKey));
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = mSQLiteOpenHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("Cursor rawQuery(String sql, String[] whereArgs)"), e);
            }
        } finally {
            if (Utils.isDebug()) {
                Utils.log4SQL(this, entityLog("Cursor rawQuery(String sql, String[] whereArgs)"),
                        "\nsql: ", sql,
                        "\nselectionArgs: ", Utils.toString(selectionArgs),
                        "\ncursor: ", cursor
                );
            }
            return cursor;
        }
    }

    @Override
    public List<T> queryAll() {
        String log = null;
        if (Utils.isDebug()) {
            log = "List<T> queryAll()";
        }
        return queryMany(log, null, null);
    }

    @Override
    public boolean update(T entity) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean update(T entity)";
        }
        ContentValues values = toContentValues(entity);
        return update(log, values, PRIMARY_KEY_WHERE_CLAUSE, primaryKeyWhereArgs(values.getAsString(mPrimaryKey)));
    }

    @Override
    public boolean delete(String primaryKey) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean delete(String primaryKey)";
        }
        return delete(log, PRIMARY_KEY_WHERE_CLAUSE, primaryKeyWhereArgs(primaryKey));
    }

    @Override
    public boolean insertOrUpdate(T entity) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean insertOrUpdate(T entity)";
        }
        ContentValues values = toContentValues(entity);
        final String primaryKey = values.getAsString(mPrimaryKey);
        final String[] whereArgs = primaryKeyWhereArgs(primaryKey);
        boolean has =  has(null, PRIMARY_KEY_WHERE_CLAUSE, whereArgs);
        if (has) {
            return update(log, values, PRIMARY_KEY_WHERE_CLAUSE, whereArgs);
        }
        return insert(log, values);
    }

    @Override
    public boolean insertOrUpdate(T entity, String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean insertOrUpdate(T entity, String whereClause, String[] whereArgs)";
        }
        boolean has =  has(null, whereClause, whereArgs);
        ContentValues values = toContentValues(entity);
        if (has) {
            return update(log, values, whereClause, whereArgs);
        }
        return insert(log, values);
    }

    @Override
    public boolean exeTransaction(Action action) {
        SQLiteDatabase db = null;
        boolean success = false;
        try {
            db = mSQLiteOpenHelper.getWritableDatabase();
            db.beginTransaction();
            action.action(db);
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean exeTransaction(Action action)"), e);
            }
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            if (Utils.isDebug()) {
                Utils.log4SQL(this, entityLog("boolean exeTransaction(Action action)"), ", \nresult: ", success);
            }
            return success;
        }
    }
}
