package com.kejunyao.db;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * 防止{@link Context}泄漏的多处理Callback
 *
 * @author kejunyao
 * @since 2018年12月29日
 */
public abstract class NoLeakDaoProcessCallback<T> extends SafelyDaoCallback<T>
        implements DaoProcessCallback<T> {

    private final WeakReference<Context> ref;

    public NoLeakDaoProcessCallback(Context context) {
        if (context == null) {
            ref = null;
        } else {
            ref = new WeakReference<>(context);
        }
    }

    @Override
    protected final boolean isSafely() {
        if (ref != null) {
            Context context = ref.get();
            return !Utils.isFinishing(context);
        }
        return false;
    }

}