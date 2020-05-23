package com.kejunyao.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * {@link SQLiteOpenHelper}实现数据库操作基类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public abstract class AbstractSQLiteDaoImpl<T> extends AbstractDefaultDao<T> {

    private final ContentResolver mContentResolver;
    private final String mProviderAuthority;

    public AbstractSQLiteDaoImpl() {
        super();
        mContentResolver = null;
        mProviderAuthority = null;
    }

    @Deprecated
    void setContentResolver(ContentResolver resolver) {
    }

    @Deprecated
    void setProviderAuthority(String authority) {
    }

    @Override
    protected boolean has(String log, String whereClause, String[] whereArgs) {
        boolean success = false;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), new String[] {Utils.count()}, whereClause, whereArgs, null, null, null);
            if (c != null && !c.isClosed() && c.moveToFirst()) {
                success = c.getLong(0) > 0;
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog(log == null ? "boolean has(String log, String whereClause, String[] whereArgs)" : log), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (log != null) {
                Utils.log4SQL(
                        this,
                        entityLog(log),
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    protected boolean insert(String log, ContentValues values) {
        boolean success = false;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            if (isPrimaryKeyAutoIncrement) {
                values.remove(mPrimaryKey);
            }
            long id = db.insert(getTableName(), null, values);
            success = id >= 0;
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean insert(String log, ContentValues values)"), ", id: ", id);
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog(log == null ? "boolean insert(String log, ContentValues values)" : log), e);
            }
        } finally {
            if (log != null) {
                Utils.log4SQL(
                        this,
                        entityLog(log),
                        "\nentity: ", values,
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    protected T query(String log, String whereClause, String[] whereArgs) {
        T entity = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), mColumns, whereClause, whereArgs, null, null, null);
            if (c != null && !c.isClosed() && c.moveToFirst()) {
                entity = toEntity(c);
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog(log == null ? "T query(String log, String whereClause, String[] whereArgs)" : log), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (log != null) {
                String result;
                if (entity == null) {
                    result = "null";
                } else {
                    result = Utils.toString(toContentValues(entity));
                }
                Utils.log4SQL(
                        this,
                        entityLog(log),
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", result
                );
            }
            return entity;
        }
    }

    @Override
    protected List<T> queryMany(String log, String whereClause, String[] whereArgs) {
        List<T> entities = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), mColumns, whereClause, whereArgs, null, null, null);
            entities = queryMany(c);
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog(log == null ? "List<T> queryMany(String whereClause, String[] whereArgs)" : log), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (log != null) {
                StringBuilder logBuilder = new StringBuilder();
                if (entities == null || entities.size() == 0) {
                    logBuilder.append("null");
                } else {
                    for (T entity : entities) {
                        logBuilder.append(toContentValues(entity)).append('\n');
                    }
                }
                Utils.log4SQL(
                        this,
                        entityLog(log),
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", logBuilder.toString()
                );
            }
            return entities;
        }
    }

    @Override
    protected boolean update(String log, ContentValues values, String whereClause, String[] whereArgs) {
        boolean success = false;
        try {
            values.remove(mPrimaryKey);
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            db.update(getTableName(), values, whereClause, whereArgs);
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog(log == null ? "boolean update(ContentValues values, String whereClause, String[] whereArgs)" : log), e);
            }
        } finally {
            if (log != null) {
                Utils.log4SQL(
                        this,
                        entityLog(log),
                        "\nvalues: ", values,
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    protected boolean delete(String log, String whereClause, String[] whereArgs) {
        boolean success = false;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            int count = db.delete(getTableName(), whereClause, whereArgs);
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("delete"), ", count: ", count);
            }
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog(log == null ? "boolean delete(String whereClause, String[] whereArgs)" : log), e);
            }
        } finally {
            if (log != null) {
                Utils.log4SQL(
                        this,
                        entityLog(log),
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    public T query(String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "T query(String whereClause, String[] whereArgs)";
        }
        return query(log, whereClause, whereArgs);
    }

    @Override
    public List<T> queryMany(String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "List<T> queryMany(String whereClause, String[] whereArgs)";
        }
        return queryMany(log, whereClause, whereArgs);
    }

    @Override
    public boolean insert(T entity) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean insert(T entity)";
        }
        return insert(log, toContentValues(entity));
    }

    @Override
    public boolean delete(String[] primaryKeys) {
        if (primaryKeys == null || primaryKeys.length == 0) {
            if (Utils.isDebug()) {
                Utils.log4SQL(this, entityLog("boolean delete(long[] ids)"), ", ids is null or empty!");
            }
            return false;
        }
        final int SIZE = primaryKeys.length;
        StringBuilder whereClause = new StringBuilder(" DELETE FROM ").append(getTableName()).append(" WHERE ").append(mPrimaryKey).append(" IN ( ");
        String[] whereArgs = new String[SIZE];
        for (int i = 0; i < SIZE; i++) {
            if (i > 0) {
                whereClause.append(',');
            }
            whereClause.append(primaryKeys[i]);
        }
        whereClause.append(" ) ");
        boolean success = delete(whereClause.toString(), whereArgs);
        if (Utils.isDebug()) {
            Utils.log4SQL(
                    this,
                    entityLog("boolean delete(String[] primaryKeys)"),
                    "\nprimaryKeys: ", Utils.toString(primaryKeys),
                    "\nreturn: ", success
            );
        }
        return success;
    }

    @Override
    public boolean update(T entity, String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean update(T entity, String whereClause, String[] whereArgs)";
        }
        return update(log, toContentValues(entity), whereClause, whereArgs);
    }

    @Override
    public boolean delete(String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean delete(String whereClause, String[] whereArgs)";
        }
        return delete(log ,whereClause, whereArgs);
    }

    @Override
    public boolean batchInsert(List<T> entities) {
        boolean success = false;
        StringBuilder logBuilder = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            StringBuilder batchSQL = new StringBuilder();
            batchSQL.append(" INSERT INTO ").append(getTableName()).append(" ( ");
            List<String> columns = new ArrayList<>(Arrays.asList(mColumns));
            if (isPrimaryKeyAutoIncrement) {
                columns.remove(mPrimaryKey);
            }
            final int size = columns.size();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    batchSQL.append(", ");
                }
                batchSQL.append(columns.get(i));
            }
            batchSQL.append(" ) VALUES( ");
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    batchSQL.append(", ");
                }
                batchSQL.append('?');
            }
            batchSQL.append(')');

            SQLiteStatement statement = db.compileStatement(batchSQL.toString());
            logBuilder = Utils.newLogBuilder();
            for (T entity : entities) {
                ContentValues values = toContentValues(entity);
                if (logBuilder != null) {
                    logBuilder.append(values);
                }
                if (isPrimaryKeyAutoIncrement) {
                    values.remove(mPrimaryKey);
                }
                for (int i = 0; i < size; i++) {
                    statement.bindString(i + 1, values.getAsString(columns.get(i)));
                }
                long id = statement.executeInsert();
                if (Utils.isDebug()) {
                    Utils.log(this, entityLog("boolean batchInsert(List<T> entities)"), ", id: ", id, ", values: ", values.toString());
                }
            }
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean batchInsert(List<T> entities)"), e);
            }
        } finally {
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("boolean batchInsert(List<T> entities)"),
                        "\nentities: ", logBuilder,
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    public boolean batchUpdate(List<T> entities) {
        boolean success = false;
        StringBuilder logBuilder = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            StringBuilder batchSQL = new StringBuilder();
            batchSQL.append(" UPDATE ").append(getTableName()).append(" SET ");
            List<String> columns = new ArrayList<>(Arrays.asList(mColumns));
            columns.remove(mPrimaryKey);
            final int size = columns.size();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    batchSQL.append(", ");
                }
                batchSQL.append(columns.get(i)).append('=').append('?');
            }
            batchSQL.append(" WHERE ").append(mPrimaryKey).append(" = ? ");
            SQLiteStatement statement = db.compileStatement(batchSQL.toString());
            logBuilder = Utils.newLogBuilder();
            for (T entity : entities) {
                ContentValues values = toContentValues(entity);
                for (int i = 0; i < size; i++) {
                    statement.bindString(i + 1, values.getAsString(columns.get(i)));
                }
                statement.bindLong(size + 1, values.getAsLong(mPrimaryKey));
                statement.executeUpdateDelete();
                if (logBuilder != null) {
                    logBuilder.append(values);
                }
            }
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean batchUpdate(List<T> entities)"), e);
            }
        } finally {
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("boolean batchUpdate(List<T> entities)"),
                        "\nentities: ", logBuilder,
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    public boolean batchUpdate(List<ContentValues> values, String column) {
        boolean success = false;
        List<ContentValues> list = new ArrayList<>(values);
        SQLiteDatabase db = null;
        StringBuilder logBuilder = null;
        try {
            db = mSQLiteOpenHelper.getWritableDatabase();
            db.beginTransaction();
            logBuilder = Utils.newLogBuilder();
            for (ContentValues contentValues : list) {
                Assert.isTrue(contentValues.containsKey(column), "can not contains " + column);
                if (logBuilder != null) {
                    logBuilder.append(contentValues);
                }
                contentValues.remove(column);
                contentValues.remove(mPrimaryKey);
                db.update(
                        getTableName(),
                        contentValues,
                        Utils.equal(column),
                        new String[] {contentValues.getAsString(column)}
                );
            }
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean batchUpdateById(List<ContentValues> values, String column)"), e);
            }
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("boolean batchUpdate(List<ContentValues> values, String column)"),
                        "\nvalues: ", logBuilder,
                        "\ncolumn: ", column,
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    public boolean update(String[] primaryKeys, ContentValues values) {
        boolean success = false;
        try {
            values.remove(mPrimaryKey);
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            StringBuilder sql = new StringBuilder();
            sql.append(" UPDATE ").append(getTableName()).append(" SET ");
            Set<String> columns = values.keySet();
            int j = 0;
            for (String column : columns) {
                if (j > 0) {
                    sql.append(',');
                }
                sql.append(column).append(" = '").append(values.getAsString(column)).append("' ");
                j += 1;
            }
            sql.append(" WHERE ").append(mPrimaryKey).append(" IN(");
            for (int i = 0, size = primaryKeys.length; i < size; i++) {
                if (i > 0) {
                    sql.append(',');
                }
                sql.append(primaryKeys[i]);
            }
            sql.append(")");
            db.execSQL(sql.toString());
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean update(String[] primaryKeys, ContentValues values)"), e);
            }
        } finally {
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("boolean update(String[] primaryKeys, ContentValues values)"),
                        "\nprimaryKeys: ", Utils.toString(primaryKeys),
                        "\nvalues: ", values,
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    public boolean update(ContentValues values, String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean update(ContentValues values, String whereClause, String[] whereArgs)";
        }
        return update(log, values, whereClause, whereArgs);
    }

    @Override
    public boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs) {
        boolean success = false;
        StringBuilder logBuilder = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            StringBuilder batchSQL = new StringBuilder();
            batchSQL.append(" UPDATE ").append(getTableName()).append(" SET ");
            List<String> columns = new ArrayList<>(Arrays.asList(mColumns));
            columns.remove(mPrimaryKey);
            final int size = columns.size();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    batchSQL.append(", ");
                }
                batchSQL.append(columns.get(i)).append('=').append('?');
            }
            if (!TextUtils.isEmpty(whereClause)) {
                batchSQL.append(" WHERE ").append(whereClause);
            }
            final int whereArgsSize = whereArgs == null ? 0 : whereArgs.length;

            SQLiteStatement statement = db.compileStatement(batchSQL.toString());
            logBuilder = Utils.newLogBuilder();
            for (T entity : entities) {
                ContentValues values = toContentValues(entity);
                values.remove(mPrimaryKey);
                if (logBuilder != null) {
                    logBuilder.append(values);
                }
                for (int i = 0; i < size; i++) {
                    statement.bindString(i + 1, values.getAsString(columns.get(i)));
                }
                if (whereArgsSize > 0) {
                    for (int i = 0; i < whereArgsSize; i++) {
                        statement.bindString(size + i + 1, whereArgs[i]);
                    }
                }
                int count = statement.executeUpdateDelete();
                if (Utils.isDebug()) {
                    Utils.log(this, entityLog("boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs)"), ", count: ", count, "values: ", values.toString());
                }
            }
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs)"),
                        "\nentities: ", logBuilder,
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", success
                );
            }
            return success;
        }
    }

    @Override
    public Long getLong(String columnOrExpression, String whereClause, String[] whereArgs) {
        Long result = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), new String[] {columnOrExpression}, whereClause, whereArgs, null, null, null);
            if (c != null && !c.isClosed() && c.moveToFirst()) {
                result = c.getLong(0);
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("long getLong(String columnOrExpression, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("long getLong(String columnOrExpression, String whereClause, String[] whereArgs)"),
                        "\ncolumnOrExpression: ", columnOrExpression,
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", result
                );
            }
            return result;
        }
    }

    @Override
    public String getString(String columnOrExpression, String whereClause, String[] whereArgs) {
        String result = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), new String[] {columnOrExpression}, whereClause, whereArgs, null, null, null);
            if (c != null && !c.isClosed() && c.moveToFirst()) {
                result = c.getString(0);
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("String getString(String columnOrExpression, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("String getString(String columnOrExpression, String whereClause, String[] whereArgs)"),
                        "\ncolumnOrExpression: ", columnOrExpression,
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", result
                );
            }
            return result;
        }
    }

    @Override
    public List<Long> getLongs(String columnOrExpression, String whereClause, String[] whereArgs) {
        List<Long> result = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), new String[] {columnOrExpression}, whereClause, whereArgs, null, null, null);
            int count = Utils.getCount(c);
            if (count > 0) {
                result = new ArrayList<>(count);
                while (c.moveToNext()) {
                    result.add(c.getLong(0));
                }
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("List<Long> getLongs(String columnOrExpression, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("List<Long> getLongs(String columnOrExpression, String whereClause, String[] whereArgs)"),
                        "\ncolumnOrExpression: ", columnOrExpression,
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", result
                );
            }
            return result;
        }
    }

    @Override
    public List<String> getStrings(String columnOrExpression, String whereClause, String[] whereArgs) {
        List<String> result = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), new String[] {columnOrExpression}, whereClause, whereArgs, null, null, null);
            int count = Utils.getCount(c);
            if (count > 0) {
                result = new ArrayList<>(count);
                while (c.moveToNext()) {
                    result.add(c.getString(0));
                }
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("List<String> getStrings(String columnOrExpression, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("List<String> getStrings(String columnOrExpression, String whereClause, String[] whereArgs)"),
                        "\ncolumnOrExpression: ", columnOrExpression,
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", result
                );
            }
            return result;
        }
    }

    /**
     * 获取某行数据的部分或全部值
     * @param columnsOrExpressions 多列或多表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return columnsOrExpressions对应的值
     */
    public ContentValues getRowValues(String[] columnsOrExpressions, String whereClause, String[] whereArgs) {
        ContentValues result = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(getTableName(), columnsOrExpressions, whereClause, whereArgs, null, null, null);
            if (c != null && !c.isClosed() && c.moveToFirst()) {
                result = new ContentValues();
                for (String column : columnsOrExpressions) {
                    Utils.putCursorValue(result, c, column);
                }
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("ContentValues getRowValues(String[] columnsOrExpressions, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            Utils.closeSafely(c);
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("ContentValues getRowValues(String[] columnsOrExpressions, String whereClause, String[] whereArgs)"),
                        "\ncolumnsOrExpressions: ", Utils.toString(columnsOrExpressions),
                        "\nwhereClause: ", whereClause,
                        "\nwhereArgs: ", Utils.toString(whereArgs),
                        "\nreturn: ", result
                );
            }
            return result;
        }
    }

    @Override
    protected Cursor query(String tableName, String[] columns, String whereClause, String[] whereArgs) {
        Cursor c = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
            c = db.query(tableName, columns, whereClause, whereArgs, null, null, null);
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("Cursor query(Uri uri, String[] columns, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            return c;
        }
    }
}