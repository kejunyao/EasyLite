package com.kejunyao.db;

/**
 * 安全回调
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public abstract class SafelyDaoCallback<T> implements DaoCallback<T> {

    protected abstract boolean isSafely();

    void onCallbackSafely(T t) {
        if (isSafely()) {
            onCallback(t);
        }
    }
}
