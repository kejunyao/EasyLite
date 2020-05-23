package com.kejunyao.db;

import android.text.TextUtils;

/**
 * 断言
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public final class Assert {

    private Assert() {
    }

    public static void isTrue(boolean is, String msg) {
        if (!is) {
            throw new IllegalArgumentException(TextUtils.isEmpty(msg) ? " is false" : msg);
        }
    }

    public static void notEmpty(String string, String msg) {
        if (string == null || string.length() == 0) {
            throw new IllegalArgumentException(TextUtils.isEmpty(msg) ? "can not empty" : msg);
        }
    }

    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(TextUtils.isEmpty(msg) ? "can not be null" : msg);
        }
    }

    public static void isNull(Object obj, String msg) {
        if (obj != null) {
            throw new IllegalArgumentException(TextUtils.isEmpty(msg) ? (obj.toString() + " must be null.") : msg);
        }
    }
}
