package com.kejunyao.db;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link android.content.ContentProvider}实现数据库操作基类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public abstract class AbstractProviderDaoImpl<T> extends AbstractDefaultDao<T> {

    public AbstractProviderDaoImpl() {
    }

    @Override
    void setProviderAuthority(String authority) {
        mProviderAuthority = authority;
    }

    private Uri mUri;
    protected Uri getUri() {
        if (mUri == null) {
            mUri = uri(getTableName());
        }
        return mUri;
    }

    protected Uri uri(String tableName) {
        return Uri.parse(Utils.concat("content://", mProviderAuthority, "/", tableName));
    }

    protected boolean isProviderEnable(Uri uri) {
        boolean has = mContentResolver != null
                && uri != null
                && mContentResolver.acquireContentProviderClient(uri) != null;
        if (!has) {
            if (Utils.isDebug()) {
                Utils.log(this, "isProviderEnable, no uri: ", uri);
            }
        }
        return has;
    }

    private boolean isProviderEnable() {
        return isProviderEnable(getUri());
    }

    @Override
    protected boolean has(String log, String whereClause, String[] whereArgs) {
        if (!isProviderEnable()) {
            return false;
        }
        Cursor c = null;
        boolean result = false;
        try {
            c = mContentResolver.query(getUri(), new String[]{Utils.count()}, whereClause, whereArgs, null);
            if (c != null && !c.isClosed() && c.moveToFirst()) {
                result = c.getLong(0) > 0l;
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(
                        this,
                        entityLog(log == null ? "boolean has(boolean printLog, String whereClause, String[] whereArgs)" : log),
                        e
                );
            }
        } finally {
            Utils.closeSafely(c);
            if (log != null) {
                Utils.log4SQL(this, entityLog(log), "\nreturn: ", result);
            }
            return result;
        }
    }

    @Override
    protected boolean insert(String log, ContentValues values) {
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        try {
            if (isPrimaryKeyAutoIncrement) {
                values.remove(mPrimaryKey);
            }
            Uri uri = mContentResolver.insert(getUri(), values);
            long id = Utils.getInsertId(uri);
            success = id > 0;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog(log == null ? "boolean insert(T entity)" : log), e);
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
        if (!isProviderEnable()) {
            return null;
        }
        T entity = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(
                    getUri(),
                    mColumns,
                    whereClause,
                    whereArgs,
                    null
            );
            if (c != null && !c.isClosed() && c.moveToFirst()) {
                entity = toEntity(c);
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(
                        this,
                        entityLog(log == null ? "T query(String whereClause, String[] whereArgs)" : log),
                        e
                );
            }
        } finally {
            Utils.closeSafely(c);
            if (Utils.isDebug()) {
                String result;
                if (entity == null) {
                    result = "null";
                } else {
                    result = Utils.toString(toContentValues(entity));
                }
                if (log != null) {
                    Utils.log4SQL(
                            this,
                            entityLog(log),
                            "\nwhereClause: ", whereClause,
                            "\nwhereArgs: ", Utils.toString(whereArgs),
                            "\nreturn: ", result
                    );
                }
            }
            return entity;
        }
    }

    @Override
    protected List<T> queryMany(String log, String whereClause, String[] whereArgs) {
        if (!isProviderEnable()) {
            return null;
        }
        List<T> entities = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(
                    getUri(),
                    mColumns,
                    whereClause,
                    whereArgs,
                    null
            );
            entities = queryMany(c);
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(
                        this,
                        entityLog(log == null ? "List<T> queryMany(String whereClause, String[] whereArgs)" : log),
                        e
                );
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
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        try {
            if (mPrimaryKey != null) {
                values.remove(mPrimaryKey);
            }
            int row = mContentResolver.update(getUri(), values, whereClause, whereArgs);
            success = row > 0;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(
                        this,
                        entityLog(log == null ? "boolean update(T entity, String whereClause, String[] whereArgs)" : log),
                        e
                );
            }
        } finally {
            if (log != null) {
                Utils.log4SQL(
                        this,
                        entityLog(log),
                        "\nentity: ", values,
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
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        try {
            int number = mContentResolver.delete(getUri(), whereClause, whereArgs);
            success = number > 0;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(
                        this,
                        entityLog(log == null ? "boolean delete(String whereClause, String[] whereArgs)" : log),
                        e
                );
            }
        } finally {
            if (log != null) {
                Utils.log4SQL(
                        this,
                        log,
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
    public boolean update(T entity, String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean update(T entity, String whereClause, String[] whereArgs)";
        }
        return update(log, toContentValues(entity), whereClause, whereArgs);
    }

    @Override
    public boolean delete(String[] primaryKeys) {
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        try {
            for (String primaryKey : primaryKeys) {
                ops.add(ContentProviderOperation.newDelete(getUri()).withSelection(PRIMARY_KEY_WHERE_CLAUSE, primaryKeyWhereArgs(primaryKey)).build());
            }
            mContentResolver.applyBatch(mProviderAuthority, ops);
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean delete(String[] primaryKeys)"), e);
            }
        } finally {
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
    }

    @Override
    public boolean delete(String whereClause, String[] whereArgs) {
        String log = null;
        if (Utils.isDebug()) {
            log = "boolean delete(String whereClause, String[] whereArgs)";
        }
        return delete(log, whereClause, whereArgs);
    }

    @Override
    public boolean batchInsert(List<T> entities) {
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        StringBuilder logBuilder = null;
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            logBuilder = Utils.newLogBuilder();
            for (T entity : entities) {
                ContentValues values = toContentValues(entity);
                if (logBuilder != null) {
                    logBuilder.append(values);
                }
                if (isPrimaryKeyAutoIncrement) {
                    values.remove(mPrimaryKey);
                }
                ops.add(ContentProviderOperation.newInsert(getUri()).withValues(values).build());
            }
            mContentResolver.applyBatch(mProviderAuthority, ops);
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
    public boolean batchUpdate(List<ContentValues> values, String column) {
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        List<ContentValues> list = new ArrayList<>(values);
        StringBuilder logBuilder = null;
        try {
            logBuilder = Utils.newLogBuilder();
            for (ContentValues cv : list) {
                Assert.isTrue(cv.containsKey(column), Utils.concat("not contains ", column));
                ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(getUri());
                builder.withSelection(Utils.equal(column), new String[] {cv.getAsString(column)});
                cv.remove(column);
                cv.remove(mPrimaryKey);
                builder.withValues(cv);
                ops.add(builder.build());
                if (logBuilder != null) {
                    logBuilder.append(cv);
                }
            }
            mContentResolver.applyBatch(mProviderAuthority, ops);
            success = true;

        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean batchUpdate(List<ContentValues> values, String column)"), e);
            }
        } finally {
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
    public boolean batchUpdate(List<T> entities) {
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        StringBuilder logBuilder = null;
        try {
            if (Utils.isDebug()) {
                logBuilder = new StringBuilder();
            }
            for (T entity : entities) {
                ContentValues values = toContentValues(entity);
                ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(getUri());
                builder.withSelection(PRIMARY_KEY_WHERE_CLAUSE, primaryKeyWhereArgs(values.getAsString(mPrimaryKey)));
                values.remove(mPrimaryKey);
                builder.withValues(values);
                ops.add(builder.build());
                if (logBuilder != null) {
                    logBuilder.append(values);
                }
            }
            mContentResolver.applyBatch(mProviderAuthority, ops);
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
    public boolean update(String[] primaryKeys, ContentValues values) {
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        try {
            values.remove(mPrimaryKey);
            for (String primaryKey : primaryKeys) {
                ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(getUri());
                builder.withValues(values);
                builder.withSelection(PRIMARY_KEY_WHERE_CLAUSE, primaryKeyWhereArgs(primaryKey));
                ops.add(builder.build());
            }
            mContentResolver.applyBatch(mProviderAuthority, ops);
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean update(List<Long> ids, ContentValues values)"), e);
            }
        } finally {
            if (Utils.isDebug()) {
                Utils.log4SQL(
                        this,
                        entityLog("boolean update(List<Long> ids, ContentValues values)"),
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
        if (!isProviderEnable()) {
            return false;
        }
        boolean success = false;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        StringBuilder logBuilder = null;
        try {
            logBuilder = Utils.newLogBuilder();
            for (T entity : entities) {
                ContentValues values = toContentValues(entity);
                if (logBuilder != null) {
                    logBuilder.append(values);
                }
                ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(getUri());
                builder.withSelection(whereClause, whereArgs);
                values.remove(mPrimaryKey);
                builder.withValues(values);
                ops.add(builder.build());
            }
            mContentResolver.applyBatch(mProviderAuthority, ops);
            success = true;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            if (Utils.isDebug()) {
                Utils.log4SQL(this, entityLog("boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs)"),
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
        if (!isProviderEnable()) {
            return null;
        }
        Long result = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(getUri(), new String[] {columnOrExpression}, whereClause, whereArgs, null);
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
        if (!isProviderEnable()) {
            return null;
        }
        String result = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(getUri(), new String[] {columnOrExpression}, whereClause, whereArgs, null);
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
        if (!isProviderEnable()) {
            return null;
        }
        List<Long> result = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(getUri(), new String[] {columnOrExpression}, whereClause, whereArgs, null);
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
                Utils.log4SQL(this, entityLog("List<Long> getLongs(String columnOrExpression, String whereClause, String[] whereArgs)"),
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
        if (!isProviderEnable()) {
            return null;
        }
        List<String> result = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(getUri(), new String[] {columnOrExpression}, whereClause, whereArgs, null);
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
        if (!isProviderEnable()) {
            return null;
        }
        ContentValues result = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(getUri(), columnsOrExpressions, whereClause, whereArgs, null);
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
        Uri uri = uri(tableName);
        if (!isProviderEnable(uri)) {
            return null;
        }
        Cursor c = null;
        try {
            c = mContentResolver.query(
                    uri,
                    columns,
                    whereClause,
                    whereArgs,
                    null
            );
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, entityLog("Cursor query(Uri uri, String[] columns, String whereClause, String[] whereArgs)"), e);
            }
        } finally {
            return c;
        }
    }
}
