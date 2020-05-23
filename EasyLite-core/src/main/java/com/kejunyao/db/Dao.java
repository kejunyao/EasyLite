package com.kejunyao.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

/**
 * EasyDao的通用接口<br/>
 * 其中，方法带有{@link DaoCallback}类型参数的，为异步操作，<br/>
 * 若方法中{@link DaoCallback}参数为{@link UIDaoCallback}则表示回调在主线程中进行。
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public interface Dao<T> {

    /**
     * 按主键判断记录是否存在
     * @param primaryKey 主键值
     * @return true，存在；false，不存在
     */
    boolean has(String primaryKey);

    /**
     * 判断满足某条件的记录是否存在
     * @param whereClause 查询条件
     * @param whereArgs 查询条件参数
     * @return true，记录存在；false，记录不存在
     */
    boolean has(String whereClause, String[] whereArgs);

    /**
     * 按主键查询记录
     * @param primaryKey 主键值
     * @return 实体类
     */
    T query(String primaryKey);

    /**
     * 查询符合条件的某条记录
     * @param whereClause 查询条件
     * @param whereArgs 查询条件中的参数
     * @return 实体类
     */
    T query(String whereClause, String[] whereArgs);

    /**
     * 万能查询
     * @param sql sql语句
     * @param selectionArgs 条件参数
     * @return {@link Cursor}
     */
    Cursor rawQuery(String sql, String[] selectionArgs);

    /**
     * 查询符合条件的多条记录
     * @param whereClause 查询条件
     * @param whereArgs 查询条件中的参数
     * @return 符合条件的多条记录
     */
    List<T> queryMany(String whereClause, String[] whereArgs);

    /**
     * 查询某实体（T）所有记录
     * @return 所有记录
     */
    List<T> queryAll();

    /**
     * 插入实体
     * @param entity 实体
     * @return true, 插入成功；false，操作失败
     */
    boolean insert(T entity);

    /**
     * 更新记录（根据主键进行更新操作，注意参数entity的主键必须有值）
     * @param entity 实体
     * @return true，更新成功；false，更新失败
     */
    boolean update(T entity);

    /**
     * 按条件更新实体记录
     * @param entity 实体
     * @param whereClause 更新条件
     * @param whereArgs 更新条件中的参数
     * @return true，更新成功；false，更新失败
     */
    boolean update(T entity, String whereClause, String[] whereArgs);

    /**
     * 按主键范围更新记录
     * @param primaryKeys 指定范围的主键值
     * @param values 记录属性名称及对应的值
     * @return true，更新成功；false，更新失败
     */
    boolean update(String[] primaryKeys, ContentValues values);

    /**
     * 删除符合条件的记录
     * @param whereClause 删除条件
     * @param whereArgs 删除条件中的参数
     * @return true，删除成功；false，删除失败
     */
    boolean delete(String whereClause, String[] whereArgs);

    /**
     * 按主键删除记录
     * @param primaryKey 主键值
     * @return true，删除成功；false，删除失败
     */
    boolean delete(String primaryKey);

    /**
     * 按主键批量删除记录
     * @param primaryKeys 多个主键值
     * @return true，批量删除成功；false，批量删除失败
     */
    boolean delete(String[] primaryKeys);

    /**
     * 批量插入实体
     * @param entities 批量实体
     * @return true，批量插入成功；false，批量插入失败
     */
    boolean batchInsert(List<T> entities);

    /**
     * 批量更新实体
     * @param entities 批量实体
     * @return true，批量更新成功；false，批量更新失败
     */
    boolean batchUpdate(List<T> entities);

    /**
     * 按条件更新记录部分属性的信息
     * @param values 记录要更新的字段名称及对应的值
     * @param whereClause 更新条件
     * @param whereArgs 更新条件中的参数
     * @return true，更新成功；false，更新失败
     */
    boolean update(ContentValues values, String whereClause, String[] whereArgs);

    /**
     * 用column作为更新条件，批量更新记录的某些属性
     * @param values 记录要更新的字段名称及对应的值
     * @param column 字段，其中column对应的值，可以通过values.getString(column)获取
     * @return true，批量操作成功；false，批量操作失败
     */
    boolean batchUpdate(List<ContentValues> values, String column);

    /**
     * 按条件批量更新
     * @param entities 批量实体
     * @param whereClause 更新条件
     * @param whereArgs 更新条件中的参数
     * @return true，批量更新成功；false，批量更新失败
     */
    boolean batchUpdate(List<T> entities, String whereClause, String[] whereArgs);

    /**
     * 插入或更新
     * @param entity 实体
     * @return true，插入或更新成功；false，插入或更新失败
     */
    boolean insertOrUpdate(T entity);

    /**
     * 按条件插入或更新
     * @param entity 实体
     * @param whereClause 插入或更新条件
     * @param whereArgs 插入或更新条件中的参数
     * @return 插入或更新成功；false，插入或更新失败
     */
    boolean insertOrUpdate(T entity, String whereClause, String[] whereArgs);

    /**
     * 查询column值、求和（SUM(column))、最大值（MAX(column)）等等（仅一条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return 查询结果
     */
    Long getLong(String columnOrExpression, String whereClause, String[] whereArgs);

    /**
     * 获取行某列的文本（仅一条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return 取行某列的文本
     */
    String getString(String columnOrExpression, String whereClause, String[] whereArgs);

    /**
     * 查询column值、求和（SUM(column))、最大值（MAX(column)）等等（多条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return
     */
    List<Long> getLongs(String columnOrExpression, String whereClause, String[] whereArgs);

    /**
     * 获取行某列的文本（多条记录）
     * @param columnOrExpression 列名或表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return 取行某列的文本
     */
    List<String> getStrings(String columnOrExpression, String whereClause, String[] whereArgs);

    /**
     * 获取某行数据的部分或全部值
     * @param columnsOrExpressions 多列或多表达式
     * @param whereClause 查询条件
     * @param whereArgs 查询参数
     * @return columnsOrExpressions对应的值
     */
    ContentValues getRowValues(String[] columnsOrExpressions, String whereClause, String[] whereArgs);

    /**
     * 执行事务（非注解方式）
     * @param action {@link Action}
     * @return true，事务执行成功；false，事务执行失败
     */
    boolean exeTransaction(Action action);
}
