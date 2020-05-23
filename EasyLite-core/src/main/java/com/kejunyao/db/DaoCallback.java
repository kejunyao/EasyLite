package com.kejunyao.db;

/**
 * 回调基类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public interface DaoCallback<T> {
    void onCallback(T t);
}
