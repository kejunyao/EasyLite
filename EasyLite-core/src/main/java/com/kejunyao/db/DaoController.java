package com.kejunyao.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Dao控制器
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
final class DaoController {

    private final Dao mDao;

    private final SQLiteOpenHelper mSQLiteOpenHelper;

    DaoController(Dao dao, SQLiteOpenHelper openHelper) {
        this.mDao = dao;
        this.mSQLiteOpenHelper = openHelper;
    }

    Dao getDao() {
        return mDao;
    }

    /**
     * @see {@link Dao#has(String)}
     */
    boolean has(String primaryKey) {
        return mDao.has(primaryKey);
    }

    /**
     * @see {@link Dao#has(String, String[])}
     */
    boolean has(String whereClause, String[] whereArgs) {
        return mDao.has(whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#query(String)}
     */
    <T> T query(String primaryKey) {
        return (T) mDao.query(primaryKey);
    }

    /**
     * @see {@link Dao#query(String, String[])}
     */
    <T> T query(String whereClause, String[] whereArgs) {
        return (T) mDao.query(whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#rawQuery(String, String[])}
     */
    Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDao.rawQuery(sql, selectionArgs);
    }

    /**
     * @see {@link Dao#queryMany(String, String[])}
     */
    <T> List<T> queryMany(String whereClause, String[] whereArgs) {
        return mDao.queryMany(whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#queryAll()}
     */
    <T> List<T> queryAll() {
        return mDao.queryAll();
    }

    /**
     * @see {@link Dao#insert(T)}
     */
    <T> boolean insert(T entity) {
        return mDao.insert(entity);
    }

    /**
     * @see {@link Dao#update(T)}
     */
    <T> boolean update(T entity) {
        return mDao.update(entity);
    }

    /**
     * @see {@link Dao#update(ContentValues, String, String[])}
     */
    boolean update(ContentValues values, String whereClause, String[] whereArgs) {
        return mDao.update(values, whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#update(T, String, String[])}
     */
    <T> boolean update(T entity, String whereClause, String[] whereArgs) {
        return mDao.update(entity, whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#update(String[], ContentValues)}
     */
    boolean update(String[] primaryKeys, ContentValues values) {
        return mDao.update(primaryKeys, values);
    }

    /**
     * @see {@link Dao#delete(String)}
     */
    boolean delete(String primaryKey) {
        return mDao.delete(primaryKey);
    }

    /**
     * @see {@link Dao#delete(String[])}
     */
    boolean delete(String[] primaryKeys) {
        return mDao.delete(primaryKeys);
    }

    /**
     * @see {@link Dao#delete(String, String[])}
     */
    boolean delete(String whereClause, String[] whereArgs) {
        return mDao.delete(whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#batchInsert(List <T>)}
     */
    <T> boolean batchInsert(List<T> entities) {
        boolean success = false;
        boolean hasTransaction = AnnotationUtils.hasTransaction(mDao.getClass(), "batchInsert", List.class);
        SQLiteDatabase db = null;
        if (hasTransaction) {
            if (Utils.isDebug()) {
                Utils.log(this, mDao.getClass().getSimpleName() , ", batchInsert(List) has transaction");
            }
            db = mSQLiteOpenHelper.getWritableDatabase();
            db.beginTransaction();
        } else {
            if (Utils.isDebug()) {
                Utils.log(this, mDao.getClass().getSimpleName(), ", batchInsert(List) no transaction");
            }
        }
        try {
            boolean s = mDao.batchInsert(entities);
            if (hasTransaction) {
                db.setTransactionSuccessful();
            }
            success = s;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, "batchInsert(List)", e);
            }
        } finally {
            if (hasTransaction) {
                db.endTransaction();
            }
            return success;
        }
    }

    /**
     * @see {@link Dao#batchUpdate(List <T>)}
     */
    <T> boolean batchUpdate(List<T> entities) {
        boolean success = false;
        boolean hasTransaction = AnnotationUtils.hasTransaction(mDao.getClass(), "batchUpdate", List.class);
        SQLiteDatabase db = null;
        if (hasTransaction) {
            if (Utils.isDebug()) {
                Utils.log(this, mDao.getClass().getSimpleName(), ", batchUpdate(List) has transaction");
            }
            db = mSQLiteOpenHelper.getWritableDatabase();
            db.beginTransaction();
        } else {
            if (Utils.isDebug()) {
                Utils.log(this, mDao.getClass().getSimpleName(), ", batchUpdate(List) no transaction");
            }
        }
        try {
            boolean s = mDao.batchUpdate(entities);
            if (hasTransaction) {
                db.setTransactionSuccessful();
            }
            success = s;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, "batchUpdate(List)", e);
            }
        } finally {
            if (hasTransaction) {
                db.endTransaction();
            }
            return success;
        }
    }

    /**
     * @see {@link Dao#batchUpdate(List <T>, String , String[])}
     */
    <T> boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs) {
        boolean success = false;
        boolean hasTransaction = AnnotationUtils.hasTransaction(mDao.getClass(), "batchUpdate", List.class, String.class, String[].class);
        SQLiteDatabase db = null;
        if (hasTransaction) {
            if (Utils.isDebug()) {
                Utils.log(this, mDao.getClass().getSimpleName(), ", batchUpdate(List, String, String[]) has transaction");
            }
            db = mSQLiteOpenHelper.getWritableDatabase();
            db.beginTransaction();
        } else {
            if (Utils.isDebug()) {
                Utils.log(this, mDao.getClass().getSimpleName(), ", batchUpdate(List, String, String[]) no transaction");
            }
        }
        try {
            boolean s = mDao.batchUpdate(entities, whereClause, whereArgs);
            if (hasTransaction) {
                db.setTransactionSuccessful();
            }
            success = s;
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, "batchUpdate(List, String, String[])", e);
            }
        } finally {
            if (hasTransaction) {
                db.endTransaction();
            }
            return success;
        }
    }

    /**
     * @see {@link Dao#insertOrUpdate(T)}
     */
    <T> boolean insertOrUpdate(T entity) {
        return mDao.insertOrUpdate(entity);
    }

    /**
     * @see {@link Dao#insertOrUpdate(T, String, String[])}
     */
    <T> boolean insertOrUpdate(T entity, String whereClause, String[] whereArgs) {
        return mDao.insertOrUpdate(entity, whereClause, whereArgs);
    }

    <T> T execute(String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = AnnotationUtils.getMethod(mDao.getClass(), methodName, parameterTypes);
        if (method == null) {
            if (Utils.isDebug()) {
                Utils.log(this, mDao.getClass().getSimpleName(), ", execute no method: ", methodName);
            }
            return null;
        }
        boolean hasTransaction = AnnotationUtils.hasTransaction(mDao.getClass(), methodName, parameterTypes);
        SQLiteDatabase db = null;
        if (hasTransaction) {
            if (Utils.isDebug()) {
                Utils.log(this, methodName, ", has transaction");
            }
            db = mSQLiteOpenHelper.getWritableDatabase();
            db.beginTransaction();
        } else {
            if (Utils.isDebug()) {
                Utils.log(this, methodName, ", no transaction");
            }
        }
        T t = null;
        try {
            t = (T) method.invoke(mDao, args);
            if (hasTransaction) {
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                Utils.log(this, "execute(", methodName, ", ", args, ')', e);
            }
        } finally {
            if (hasTransaction) {
                db.endTransaction();
            }
            return t;
        }
    }

    /**
     * @see {@link Dao#batchUpdate(List < ContentValues >, String )}
     */
    boolean batchUpdate(List<ContentValues> values, String column) {
        return mDao.batchUpdate(values, column);
    }

    boolean exeTransaction(Action action) {
        return mDao.exeTransaction(action);
    }

    /**
     * @see {@link Dao#getLong(String, String, String[])}
     */
    Long getLong(String columnOrExpression, String whereClause, String[] whereArgs) {
        return mDao.getLong(columnOrExpression, whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#getString(String, String, String[])}
     */
    String getString(String column, String whereClause, String[] whereArgs) {
        return mDao.getString(column, whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#getLongs(String, String, String[])}
     */
    List<Long> getLongs(String columnOrExpression, String whereClause, String[] whereArgs) {
        return mDao.getLongs(columnOrExpression, whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#getStrings(String, String, String[])}
     */
    List<String> getStrings(String columnOrExpression, String whereClause, String[] whereArgs) {
        return mDao.getStrings(columnOrExpression, whereClause, whereArgs);
    }

    /**
     * @see {@link Dao#getRowValues(String[], String, String[])}
     */
    ContentValues getRowValues(String[] columnsOrExpressions, String whereClause, String[] whereArgs) {
        return mDao.getRowValues(columnsOrExpressions, whereClause, whereArgs);
    }

    /**
     * 检查数据库表字段的完整性
     */
    void checkTableIntegrity() {
        if (mDao instanceof AbstractDefaultDao) {
            AbstractDefaultDao dao = (AbstractDefaultDao) mDao;
            Column[] columns = dao.getColumns();
            if (columns == null || columns.length == 0) {
                return;
            }
            Cursor cursor = null;
            try {
                SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
                final String table = dao.getTableName();
                if (hasTable(table)) {
                    if (Utils.isDebug()) {
                        Utils.log(this, table, " 表存在，进行字段检查操作.......");
                    }
                    cursor = db.rawQuery("SELECT * FROM " + table, null);
                    if (cursor != null && !cursor.isClosed()) {
                        for (int i = 0, length = columns.length; i < length; i++) {
                            Column column = columns[i];
                            int index = cursor.getColumnIndex(column.name());
                            if (index < 0) {
                                try {
                                    db.execSQL(column.buildAddColumnSql(table));
                                    if (Utils.isDebug()) {
                                        Utils.log(this, table, '.', column.name(), "，添加成功！");
                                    }
                                } catch (Exception e) {
                                    if (Utils.isDebug()) {
                                        Utils.log(this, table, '.', column.name(), "，新增失败！");
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                if (Utils.isDebug()) {
                                    Utils.log(this, table, '.', column.name(), "，已存在！");
                                }
                            }
                        }
                    }
                } else {
                    if (Utils.isDebug()) {
                        Utils.log(this, table, " 表不存在，进行表添加操作");
                    }
                    dao.createTable(db);
                }
            } catch (Exception e) {
                if (Utils.isDebug()) {
                    e.printStackTrace();
                }
            } finally {
                Utils.closeSafely(cursor);
            }
        }
    }

    private boolean hasTable(String table) {
        boolean has = false;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", table});
            if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
                has = cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            if (Utils.isDebug()) {
                e.printStackTrace();
            }
        } finally {
            Utils.closeSafely(cursor);
            return has;
        }
    }
}
