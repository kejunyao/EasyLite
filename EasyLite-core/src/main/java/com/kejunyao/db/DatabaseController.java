package com.kejunyao.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Process;
import android.util.SparseArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 数据库增、删、改、查Controller
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public class DatabaseController {

    private final HashMap<Class, DaoController> mControllers = new HashMap<>();
    private SparseArray<OnDatabaseUpdateListener> mListeners;

    private final ExecutorService mExecutor;
    private final ContentResolver mContentResolver;
    private final SQLiteOpenHelper mSQLiteOpenHelper;
    private final String mProviderAuthority;

    public DatabaseController(ExecutorService executor, SQLiteOpenHelper openHelper) {
        this(executor, openHelper, null, null);
    }

    public DatabaseController(ExecutorService executor,
                              ContentResolver resolver, String providerAuthority) {
        this(executor, null, resolver, providerAuthority);
    }

    public DatabaseController(ExecutorService executor, SQLiteOpenHelper openHelper,
                              ContentResolver resolver, String providerAuthority) {
        if (executor == null) {
            this.mExecutor = Utils.newSingleFixedThreadPool();
        } else {
            this.mExecutor = executor;
        }
        this.mSQLiteOpenHelper = openHelper;
        this.mContentResolver = resolver;
        this.mProviderAuthority = providerAuthority;
    }

    /**
     * 数据库记录操作日志开关
     * @param enabled true，显示日志；false，不显示日志
     */
    public static final void setLoggingEnabled(boolean enabled) {
        Utils.setLoggingEnabled(enabled);
    }

    public DatabaseController addDatabaseUpdateListener(int currVersion, OnDatabaseUpdateListener l) {
        synchronized (this) {
            if (mListeners == null) {
                mListeners = new SparseArray<>();
            } else {
                if (mListeners.indexOfKey(currVersion) >= 0) {
                    return this;
                }
            }
            mListeners.put(currVersion, l);
        }
        return this;
    }

    public static void doCallback(final DaoCallback<Boolean> callback, final boolean success) {
        if (callback == null) {
            return;
        }
        if (callback instanceof SafelyDaoCallback) {
            Utils.executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    ((SafelyDaoCallback) callback).onCallbackSafely(success);
                }
            });
            return;
        }
        if (callback instanceof UIDaoCallback) {
            Utils.executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onCallback(success);
                }
            });
            return;
        }
        callback.onCallback(success);
    }

    public static <T> void doCallback(final DaoCallback<T> callback, final T entity) {
        if (callback == null) {
            return;
        }
        if (callback instanceof SafelyDaoCallback) {
            Utils.executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    ((SafelyDaoCallback) callback).onCallbackSafely(entity);
                }
            });
            return;
        }
        if ((callback instanceof UIDaoCallback) || (callback instanceof DaoProcessCallback)) {
            Utils.executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onCallback(entity);
                }
            });
            return;
        }
        callback.onCallback(entity);
    }

    public static <T> void doCallback(final DaoCallback<List<T>> callback, final List<T> entities) {
        if (callback == null) {
            return;
        }
        if (callback instanceof SafelyDaoCallback) {
            Utils.executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    ((SafelyDaoCallback) callback).onCallbackSafely(entities);
                }
            });
            return;
        }
        if (callback instanceof UIDaoCallback) {
            Utils.executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onCallback(entities);
                }
            });
            return;
        }
        callback.onCallback(entities);
    }

    private DaoController findDaoController(Class clazz) {
        DaoController controller = mControllers.get(clazz);
        if (controller == null) {
            for (Class key : mControllers.keySet()) {
                if (key.isAssignableFrom(clazz)) {
                    return mControllers.get(key);
                }
            }
        }
        return controller;
    }

    public void onDatabaseUpdate(SQLiteDatabase db, int currVersion) {
        if (mListeners != null) {
            OnDatabaseUpdateListener l = mListeners.get(currVersion);
            if (l != null) {
                l.onDatabaseUpdate(db, currVersion);
            }
        }
    }

    public DatabaseController addDao(Class<? extends AbstractDao> daoClazz) {
        synchronized (this) {
            try {
                AbstractDao dao = daoClazz.newInstance();
                addDao(dao);
            } catch (Exception e) {
                if (Utils.isDebug()) {
                    Utils.log(this, "addDao by clazz", e);
                }
            } finally {
                return this;
            }
        }
    }

    public DatabaseController addDao(AbstractDao dao) {
        synchronized (this) {
            addDaoController(dao);
        }
        return this;
    }

    private void addDaoController(AbstractDao dao) {
        if (!mControllers.containsKey(dao.getPersistentClass())) {
            dao.setContentResolver(mContentResolver);
            dao.setProviderAuthority(mProviderAuthority);
            dao.setSQLiteOpenHelper(mSQLiteOpenHelper);
            mControllers.put(dao.getPersistentClass(), new DaoController(dao, mSQLiteOpenHelper));
        }
    }

    public <T> void createTable(Class<T> clazz, SQLiteDatabase db) {
        synchronized (this) {
            DaoController controller = findDaoController(clazz);
            if (controller == null) {
                if (Utils.isDebug()) {
                    Utils.log(this, "the DaoController is not exist!");
                }
                return;
            }
            Dao dao = controller.getDao();
            if (dao instanceof AbstractDefaultDao) {
                ((AbstractDefaultDao) dao).createTable(db);
            } else {
                if (Utils.isDebug()) {
                    if (dao == null) {
                        Utils.log(this, "createTable failure, because the Dao is null!");
                    } else {
                        Utils.log(this, "createTable failure, because the Dao (", dao.getClass().getName(), ") is not AbstractDefaultDao instance!");
                    }
                }
            }
        }
    }

    /**
     * 表创建操作
     */
    public void createAllTables(SQLiteDatabase db) {
        synchronized (this) {
            Collection<DaoController> cs = mControllers.values();
            for (DaoController c : cs) {
                Dao dao = c.getDao();
                if (dao instanceof AbstractDefaultDao) {
                    ((AbstractDefaultDao) dao).createTable(db);
                } else {
                    if (Utils.isDebug()) {
                        if (dao == null) {
                            Utils.log(this, "createAllTables failure, because the Dao is null!");
                        } else {
                            Utils.log(this, "createAllTables failure, because the Dao (", dao.getClass().getName(), ") is not AbstractDefaultDao instance!");
                        }
                    }
                }
            }
        }
    }

    public void dropAllTables(SQLiteDatabase db) {
        Collection<DaoController> cs = mControllers.values();
        for (DaoController c : cs) {
            Dao dao = c.getDao();
            if (dao instanceof AbstractDefaultDao) {
                String tableName = ((AbstractDefaultDao) dao).getTableName();
                db.execSQL("DROP TABLE IF EXISTS " + tableName + ";");
            }
        }
    }

    /**
     * 数据库升级
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (Utils.isDebug()) {
            Utils.log(this, "DB new version = ", newVersion, ", DB old version = ", oldVersion);
        }
        synchronized (this) {
            db.beginTransaction();
            int version = oldVersion + 1;
            try {
                while (version <= newVersion) {
                    onDatabaseUpdate(db, version);
                    version++;
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                if (Utils.isDebug()) {
                    Utils.log(this, "onUpgrade, DB new version = " + newVersion + ", DB old version = " + oldVersion, e);
                }
                dropAllTables(db);
            } finally {
                db.endTransaction();
            }
        }
    }

    /**
     * 数据库降级
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        synchronized (this) {
            dropAllTables(db);
            createAllTables(db);
        }
    }

    public <T> boolean has(Class<T> clazz, long primaryKey) {
        return has(clazz, String.valueOf(primaryKey));
    }

    public <T> boolean has(Class<T> clazz, String primaryKey) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.has(primaryKey);
    }

    public <T> boolean has(Class<T> clazz, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.has(whereClause, whereArgs);
    }

    public <T> void has(final Class<T> clazz, final String whereClause,
                        final String[] whereArgs, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = has(clazz, whereClause, whereArgs);
                doCallback(callback, success);
            }
        });
    }

    public <T> T query(Class<T> clazz, long primaryKey) {
        return query(clazz, String.valueOf(primaryKey));
    }

    public <T> T query(Class<T> clazz, String primaryKey) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.query(primaryKey);
    }

    public <T> T query(Class<T> clazz, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.query(whereClause, whereArgs);
    }

    public <T> List<T> queryMany(Class<T> clazz, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.queryMany(whereClause, whereArgs);
    }

    public <T> List<T> queryAll(Class<T> clazz) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.queryAll();
    }

    /**
     * 强制指定clazz进行插入操作，主要用于T的子类操作
     */
    public <T> boolean insert(Class<T> clazz, T entity) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.insert(entity);
    }

    public <T> boolean insert(T entity) {
        DaoController controller = findDaoController(entity.getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.insert(entity);
    }

    public <T> boolean update(T entity) {
        DaoController controller = findDaoController(entity.getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.update(entity);
    }

    public <T> boolean update(T entity, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(entity.getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.update(entity, whereClause, whereArgs);
    }

    /**
     * 强制指定clazz进行更新操作，主要用于T的子类操作
     */
    public <T> boolean update(Class clazz, T entity, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.update(entity, whereClause, whereArgs);
    }

    /**
     * 通过column批量更新记录的某些属性
     * XXX 使用此方法须将，每一个{@link ContentValues}必须包含column名称及对应的值
     */
    public <T> boolean batchUpdate(Class<T> clazz, List<ContentValues> values, String column) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.batchUpdate(values, column);
    }

    public <T> boolean delete(Class<T> clazz, long primaryKey) {
        return delete(clazz, String.valueOf(primaryKey));
    }

    public <T> boolean delete(Class<T> clazz, String primaryKey) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.delete(primaryKey);
    }

    public <T> boolean delete(Class<T> clazz, List<Long> primaryKeys) {
        return delete(clazz, Utils.toArray(primaryKeys));
    }

    public <T> boolean delete(Class<T> clazz, long[] primaryKeys) {
        return delete(clazz, Utils.toArray(primaryKeys));
    }

    public <T> boolean delete(Class<T> clazz, String[] primaryKeys) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.delete(primaryKeys);
    }

    public <T> boolean delete(Class<T> clazz, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.delete(whereClause, whereArgs);
    }

    public <T> boolean batchInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        DaoController controller = findDaoController(entities.get(0).getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.batchInsert(entities);
    }

    public <T> boolean batchUpdate(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        DaoController controller = findDaoController(entities.get(0).getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.batchUpdate(entities);
    }

    public <T> boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs) {
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        DaoController controller = findDaoController(entities.get(0).getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        } else {
            return controller.batchUpdate(entities, whereClause, whereArgs);
        }
    }

    /**
     * 异步通过column批量更新记录的某些属性
     * XXX 使用此方法须将，每一个{@link ContentValues}必须包含column名称及对应的值
     */
    public <T> void batchUpdate(final Class<T> clazz, final List<ContentValues> values, final String column,
                                final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = batchUpdate(clazz, values, column);
                doCallback(callback, success);
            }
        });
    }

    public <T> void insert(final T entity, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = insert(entity);
                doCallback(callback, success);
            }
        });
    }

    public <T> void insert(final Class<T> clazz, final T entity, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = insert(clazz, entity);
                doCallback(callback, success);
            }
        });
    }

    public <T> void batchInsert(final List<T> entities, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = batchInsert(entities);
                doCallback(callback, success);
            }
        });
    }

    public <T> void query(final Class<T> clazz, final long id, final DaoCallback<T> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                T entity = query(clazz, id);
                doCallback(callback, entity);
            }
        });
    }

    public <T> void query(final Class<T> clazz,
                          final String whereClause, final String[] whereArgs,
                          final DaoCallback<T> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                T entity = query(clazz, whereClause, whereArgs);
                doCallback(callback, entity);
            }
        });
    }

    /**
     * 万能查询
     * @param sql sql语句
     * @param selectionArgs 条件参数
     * @return {@link Cursor}
     */
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        DaoController controller = null;
        for (DaoController daoController : mControllers.values()) {
            controller = daoController;
            if (controller != null) {
                break;
            }
        }
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        } else {
            return controller.rawQuery(sql, selectionArgs);
        }
    }

    /**
     * 万能查询
     * @param sql sql语句
     * @param selectionArgs 条件参数
     * @param callback {@link DaoCallback}
     */
    public void rawQuery(final String sql, final String[] selectionArgs,
                         final DaoCallback<Cursor> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Cursor cursor = rawQuery(sql, selectionArgs);
                doCallback(callback, cursor);
            }
        });
    }

    /**
     * 事务操作
     * @param action {@link Action}
     * @return true，成功；false，失败
     */
    public boolean exeTransaction(Action action) {
        synchronized (this) {
            DaoController controller = null;
            for (DaoController daoController : mControllers.values()) {
                controller = daoController;
                if (controller != null) {
                    break;
                }
            }
            if (controller == null) {
                if (Utils.isDebug()) {
                    Utils.log(this, "the DaoController is not exist!");
                }
                return false;
            } else {
                return controller.exeTransaction(action);
            }
        }
    }

    /**
     * 事务操作（异步操作）
     * @param action {@link Action}
     * @param callback {@link DaoCallback}
     */
    public void exeTransaction(final Action action, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = exeTransaction(action);
                doCallback(callback, success);
            }
        });
    }

    public <T> void queryMany(final Class<T> clazz,
                              final String whereClause, final String[] whereArgs,
                              final DaoCallback<List<T>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                List<T> entities = queryMany(clazz, whereClause, whereArgs);
                doCallback(callback, entities);
            }
        });
    }

    public <T> void queryAll(final Class<T> clazz, final DaoCallback<List<T>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                List<T> entities = queryAll(clazz);
                doCallback(callback, entities);
            }
        });
    }

    public <T> void update(final T entity, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = update(entity);
                doCallback(callback, success);
            }
        });
    }

    public boolean update(Class clazz, String[] primaryKeys, ContentValues values) {
        synchronized (this) {
            DaoController controller = findDaoController(clazz);
            if (controller == null) {
                if (Utils.isDebug()) {
                    Utils.log(this, "the DaoController is not exist!");
                }
            } else {
                return controller.update(primaryKeys, values);
            }
            return false;
        }
    }

    public boolean update(Class clazz, ContentValues values, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
        } else {
            return controller.update(values, whereClause, whereArgs);
        }
        return false;
    }

    public <T> void update(final T entity,
                           final String whereClause, final String[] whereArgs,
                           final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = update(entity, whereClause, whereArgs);
                doCallback(callback, success);
            }
        });
    }

    public <T> void batchUpdate(final List<T> entities, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = batchUpdate(entities);
                doCallback(callback, success);
            }
        });
    }

    public <T> void batchUpdate(final List<T> entities,
                                final String whereClause, final String[] whereArgs,
                                final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = batchUpdate(entities, whereClause, whereArgs);
                doCallback(callback, success);
            }
        });
    }

    public <T> void delete(final Class<T> clazz, final long id, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = delete(clazz, id);
                doCallback(callback, success);
            }
        });
    }

    public <T> void delete(final Class<T> clazz, final long[] primaryKeys, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = delete(clazz, primaryKeys);
                doCallback(callback, success);
            }
        });
    }

    public <T> void delete(final Class<T> clazz, final String[] primaryKeys, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = delete(clazz, primaryKeys);
                doCallback(callback, success);
            }
        });
    }

    public <T> void delete(final Class<T> clazz,
                           final String whereClause,
                           final String[] whereArgs,
                           final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = delete(clazz, whereClause, whereArgs);
                doCallback(callback, success);
            }
        });
    }

    public <T> void update(final Class<T> clazz,
                           final String[] primaryKeys,
                           final ContentValues values,
                           final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = update(clazz, primaryKeys, values);
                doCallback(callback, success);
            }
        });
    }

    public <T> void update(final Class<T> clazz,
                           final long[] primaryKeys,
                           final ContentValues values,
                           final DaoCallback<Boolean> callback) {
        update(clazz, Utils.toArray(primaryKeys), values, callback);
    }

    public <T> void update(final Class<T> clazz,
                           final List<Long> primaryKeys,
                           final ContentValues values,
                           final DaoCallback<Boolean> callback) {
        update(clazz, Utils.toArray(primaryKeys), values, callback);
    }

    public <T> void update(final Class<T> clazz,
                           final ContentValues values,
                           final String whereClause,
                           final String[] whereArgs,
                           final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = update(clazz, values, whereClause, whereArgs);
                if (callback != null) {
                    doCallback(callback, success);
                }
            }
        });
    }

    public <T> boolean insertOrUpdate(T entity) {
        DaoController controller = findDaoController(entity.getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.insertOrUpdate(entity);
    }

    public <T> boolean insertOrUpdate(T entity, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(entity.getClass());
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return false;
        }
        return controller.insertOrUpdate(entity, whereClause, whereArgs);
    }

    public <T> void insertOrUpdate(final T entity, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = insertOrUpdate(entity);
                doCallback(callback, success);
            }
        });
    }

    public <T> void insertOrUpdate(final T entity, final String whereClause, final String[] whereArgs, final DaoCallback<Boolean> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean success = insertOrUpdate(entity, whereClause, whereArgs);
                doCallback(callback, success);
            }
        });
    }

    public <T> boolean deleteAll(Class<T> clazz) {
        final String whereClause = null;
        final String[] whereArgs = null;
        return delete(clazz, whereClause, whereArgs);
    }

    public <T> void deleteAllAsync(final Class<T> clazz) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                final String whereClause = null;
                final String[] whereArgs = null;
                delete(clazz, whereClause, whereArgs);
            }
        });
    }

    public <T> Long getLong(Class<T> clazz, String columnOrExpression, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.getLong(columnOrExpression, whereClause, whereArgs);
    }

    public <T> void getLong(final Class<T> clazz, final String columnOrExpression, final String whereClause,
                            final String[] whereArgs, final DaoCallback<Long> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                long result = getLong(clazz, columnOrExpression, whereClause, whereArgs);
                doCallback(callback, result);
            }
        });
    }

    public <T> void getString(final Class<T> clazz, final String column, final String whereClause,
                              final String[] whereArgs, final DaoCallback<String> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                String result = getString(clazz, column, whereClause, whereArgs);
                doCallback(callback, result);
            }
        });
    }

    public <T> String getString(Class<T> clazz, String column, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.getString(column, whereClause, whereArgs);
    }

    /**
     * 查询column值、求和（SUM(column))、最大值（MAX(column)）等等（多条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     */
    public <T> List<Long> getLongs(Class<T> clazz, String columnOrExpression, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.getLongs(columnOrExpression, whereClause, whereArgs);
    }

    /**
     * 查询column值、求和（SUM(column))、最大值（MAX(column)）等等（多条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @param callback {@link DaoCallback}
     */
    public <T> void getLongs(final Class<T> clazz, final String columnOrExpression, final String whereClause,
                             final String[] whereArgs, final DaoCallback<List<Long>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                List<Long> result = getLongs(clazz, columnOrExpression, whereClause, whereArgs);
                doCallback(callback, result);
            }
        });
    }

    /**
     * 获取行某列的文本（多条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @param callback {@link DaoCallback}
     * @return 取行某列的文本
     */
    public <T> void getStrings(final Class<T> clazz, final String columnOrExpression, final String whereClause,
                               final String[] whereArgs, final DaoCallback<List<String>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                List<String> result = getStrings(clazz, columnOrExpression, whereClause, whereArgs);
                doCallback(callback, result);
            }
        });
    }

    /**
     * 获取行某列的文本（多条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return 取行某列的文本
     */
    public <T> List<String> getStrings(Class<T> clazz, String columnOrExpression, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.getStrings(columnOrExpression, whereClause, whereArgs);
    }

    /**
     * 获取某行数据的部分或全部值
     * @param columnsOrExpressions 多列或多表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return columnsOrExpressions对应的值
     */
    public <T> ContentValues getRowValues(Class<T> clazz, String[] columnsOrExpressions, String whereClause, String[] whereArgs) {
        DaoController controller = findDaoController(clazz);
        if (controller == null) {
            if (Utils.isDebug()) {
                Utils.log(this, "the DaoController is not exist!");
            }
            return null;
        }
        return controller.getRowValues(columnsOrExpressions, whereClause, whereArgs);
    }

    /**
     * 获取某行数据的部分或全部值
     * @param columnsOrExpressions 多列或多表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     */
    public <T> void getRowValues(final Class<T> clazz, final String[] columnsOrExpressions,
                                 final String whereClause, final String[] whereArgs,
                                 final DaoCallback<ContentValues> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                ContentValues result = getRowValues(clazz, columnsOrExpressions, whereClause, whereArgs);
                doCallback(callback, result);
            }
        });
    }

    /**
     * 检查数据库的完整性
     */
    public void checkDatabaseIntegrity() {
        Assert.notNull(mSQLiteOpenHelper, "SQLiteOpenHelper can not be null!");
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (Utils.isDebug()) {
                        Utils.log(DatabaseController.this, "开始数据库完整性检查................................");
                    }
                    long t = System.currentTimeMillis();
                    SQLiteDatabase db = null;
                    try {
                        db = mSQLiteOpenHelper.getWritableDatabase();
                        db.beginTransaction();
                        for (DaoController controller : mControllers.values()) {
                            controller.checkTableIntegrity();
                        }
                        db.setTransactionSuccessful();
                        if (Utils.isDebug()) {
                            Utils.log(DatabaseController.this, "checkDatabaseIntegrity, 数据库完整性操作成功");
                        }
                    } catch (Exception e) {
                        if (Utils.isDebug()) {
                            Utils.log(DatabaseController.this, "checkDatabaseIntegrity, 数据库完整性操作失败", e);
                        }
                    } finally {
                        if (db != null && db.inTransaction()) {
                            db.endTransaction();
                        }
                        if (Utils.isDebug()) {
                            Utils.log(DatabaseController.this, "数据库完整性检查耗时: ", (System.currentTimeMillis() - t), " ms");
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(final Class clazz, String methodName, Class<?>[] parameterTypes, Object[] args) {
        synchronized (this) {
            DaoController controller = findDaoController(clazz);
            if (controller == null) {
                return null;
            }
            return controller.execute(methodName, parameterTypes, args);
        }
    }

    @SuppressWarnings("unchecked")
    public void execute(final Class clazz, final String methodName, final Class<?>[] parameterTypes, final Object[] args,
                        final DaoCallback callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                doCallback(callback, execute(clazz, methodName, parameterTypes, args));
            }
        });
    }

    public void execute(final Runnable r) {
        mExecutor.execute(r);
    }

    public void executeWithLock(final Runnable r) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                synchronized (this) {
                    r.run();
                }
            }
        });
    }

    public <T> void execute(final DaoProcessCallback<T> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                synchronized (this) {
                    T entity = callback.onProcess();
                    doCallback(callback, entity);
                }
            }
        });
    }
}
