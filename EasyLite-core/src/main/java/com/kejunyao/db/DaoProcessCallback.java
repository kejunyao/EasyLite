package com.kejunyao.db;

/**
 * 多处理回调
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public interface DaoProcessCallback<T> extends DaoCallback<T> {
    T onProcess();
}
