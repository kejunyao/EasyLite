package com.kejunyao.db;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * 防止{@link Context}泄漏的Callback
 * {@link NoLeakDaoCallback#onCallback}方法在UI线程中执行
 *
 * @author kejunyao
 * @since 2018年09月21日
 */
public abstract class NoLeakDaoCallback<T> extends SafelyDaoCallback<T> {

    private final WeakReference<Context> ref;

    public NoLeakDaoCallback(Context context) {
        if (context == null) {
            ref = null;
        } else {
            ref = new WeakReference<>(context);
        }
    }

    @Override
    protected final boolean isSafely() {
        if (ref != null) {
            return !Utils.isFinishing(ref.get());
        }
        return false;
    }
}