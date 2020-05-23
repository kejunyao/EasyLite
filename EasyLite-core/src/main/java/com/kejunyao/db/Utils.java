package com.kejunyao.db;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.kejunyao.log.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

/**
 * 数据库工具类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public final class Utils {

    private static final String TAG = "EasyLite";

    private static boolean sDebug = false;

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private Utils() {
    }

    static void setLoggingEnabled(boolean enabled) {
        sDebug = enabled;
    }

    public static final boolean isDebug() {
        return sDebug;
    }

    public static final void closeSafely(Cursor c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
            }
        }
    }

    public static long getInsertId(Uri uri) {
        if (uri == null) {
            return -1l;
        }
        String idStr = uri.getLastPathSegment();
        if (TextUtils.isEmpty(idStr)) {
            return -1;
        }
        long id = -1l;
        try {
            id = Long.valueOf(idStr);
        } catch (Exception e) {
        } finally {
            return id;
        }
    }

    /**
     * 获取Cursor记录数
     * @param cursor {@link Cursor}
     * @return Cursor记录数
     */
    public static int getCount(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            return cursor.getCount();
        }
        return 0;
    }

    public static final ExecutorService newSingleFixedThreadPool() {
        return Executors.newFixedThreadPool(1);
    }

    public static final void executeOnMainThread(Runnable command) {
        HANDLER.post(command);
    }

    public static final void log(Object obj, Object... msg) {
        int msgLen = msg == null ? 0 : msg.length;
        if (msgLen == 0) {
            Log.d(TAG, "msg is empty!");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.ensureCapacity(200);
            if (obj != null) {
                sb.append(obj.getClass().getName()).append(": ");
            }
            for (int i = 0; i < msgLen; i++) {
                sb.append(msg[i]);
            }
            Log.d(TAG, sb.toString());
        }
    }

    public static final void log4SQL(Object obj, Object... msg) {
        int msgLen = msg == null ? 0 : msg.length;
        if (msgLen == 0) {
            Log.d(TAG, "msg is empty!");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.ensureCapacity(200);
            if (obj != null) {
                sb.append(obj.getClass().getName()).append('\n');
            }
            for (int i = 0; i < msgLen; i++) {
                sb.append(msg[i]);
            }
            sb.append('\n');
            sb.append("====================================================== end ======================================================");
            Log.d(TAG, sb.toString());
        }
    }

    public static final void log(Object obj, String msg, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.ensureCapacity(300);
        if (obj != null) {
            sb.append(obj.getClass().getName()).append(": ");
        }
        if (msg != null) {
            sb.append(msg).append(", ");
        }
        if (e != null) {
            sb.append(getStackTraceString(e.fillInStackTrace()));
        }
        Log.d(TAG, sb.toString());
    }

    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }

    public static String asc(String column) {
        return String.format(" ORDER BY %s ASC ", column);
    }

    public static String ascNoOrderBy(String column) {
        return String.format(" %s ASC ", column);
    }

    public static String desc(String column) {
        return String.format(" ORDER BY %s DESC ", column);
    }

    public static String groupBy(String column) {
        return String.format(" GROUP BY %s ", column);
    }

    public static String groupBy(String...columns) {
        StringBuilder sb = new StringBuilder(" GROUP BY ");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(columns[i]);
        }
        sb.append(' ');
        return sb.toString();
    }

    public static String descNoOrderBy(String column) {
        return String.format(" %s DESC ", column);
    }

    public static String limit(int start, int offset) {
        return String.format(" LIMIT %1$d, %2$d ", start, offset);
    }

    public static String limit(int start) {
        return String.format(" LIMIT %d ", start);
    }

    public static String equal(String column) {
        return String.format(" %s = ? ", column);
    }

    public static String equal(String column, String value) {
        return column + " = '" + value + "' ";
    }

    /**
     * 不等于
     */
    public static String unequal(String column) {
        return String.format(" %s <> ? ", column);
    }

    /**
     * 不等于
     */
    public static String unequal(String column, String value) {
        return String.format(" %1$s <> '%2$s' ", column, value);
    }

    /**
     * 不等于
     */
    public static String unequal(String column, int value) {
        return String.format(" %1$s <> %2$d ", column, value);
    }

    /**
     * 不等于
     */
    public static String unequal(String column, long value) {
        return String.format(" %1$s <> %2$d ", column, value);
    }

    /**
     * 等于
     */
    public static String equal(String column, long value) {
        return String.format(" %1$s = %2$d ", column, value);
    }

    /**
     * 等于
     */
    public static String equal(String column, int value) {
        return String.format(" %1$s = %2$d ", column, value);
    }

    public static String and(String condition1, String condition2) {
        return String.format(" %1$s AND %2$s ", condition1, condition2);
    }

    public static String andWithBrackets(String condition1, String condition2) {
        return String.format(" ( %1$s AND %2$s ) ", condition1, condition2);
    }

    public static String or(String condition1, String condition2) {
        return String.format(" %1$s OR %2$s ", condition1, condition2);
    }

    public static String max(String column) {
        return String.format("MAX(%s)", column);
    }

    public static String sum(String column) {
        return String.format("SUM(%s)", column);
    }

    public static String count(String column) {
        return String.format("COUNT(%s)", column);
    }

    public static String count() {
        return "COUNT(1)";
    }

    public static String in(String column, int n) {
        StringBuilder sb = new StringBuilder();
        sb.append(column).append(" IN(");
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        sb.append(')');
        return sb.toString();
    }

    public static String orWithBrackets(String condition1, String condition2) {
        return String.format(" ( %1$s OR %2$s ) ", condition1, condition2);
    }

    /**
     * 小于
     */
    public static String less(String column, long value) {
        return String.format(" %1$s < %2$d ", column, value);
    }

    /**
     * 小于
     */
    public static String less(String column, int value) {
        return String.format(" %1$s < %2$d ", column, value);
    }

    /**
     * 小于
     */
    public static String less(String column) {
        return String.format(" %s < ? ", column);
    }

    /**
     * 大于
     */
    public static String greater(String column, long value) {
        return String.format(" %1$s > %2$d ", column, value);
    }

    /**
     * 大于
     */
    public static String greater(String column, int value) {
        return String.format(" %1$s > %2$d ", column, value);
    }

    /**
     * 大于
     */
    public static String greater(String column) {
        return String.format(" %s > ? ", column);
    }

    public static String absolute() {
        return " 1 = 1 ";
    }

    public static String isNull(String column) {
        return String.format(" %s  IS NULL ", column);
    }

    public static String notIn(String column, String[] values) {
        if (TextUtils.isEmpty(column) || values == null || values.length == 0) {
            return " ";
        }
        StringBuilder sb = new StringBuilder();
        sb.ensureCapacity(300);
        sb.append(column).append(" NOT IN(");
        for (int i = 0; i < values.length; i ++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("'").append(values[i]).append("'");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String and(String... conditions) {
        if (conditions == null || conditions.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.ensureCapacity(200);
        for (int i = 0; i < conditions.length; i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            sb.append(conditions[i]).append(' ');
        }
        return sb.toString();
    }

    public static StringBuilder newLogBuilder() {
        if (sDebug) {
            return new StringBuilder();
        }
        return null;
    }

    public static String toString(String[] args) {
        if (args != null && args.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.ensureCapacity(100);
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(args[i]);
            }
            if (args.length > 1) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
            return sb.toString();
        }
        return null;
    }

    public static String toString(long[] values) {
        if (values != null && values.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.ensureCapacity(100);
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(values[i]);
            }
            if (values.length > 1) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
            return sb.toString();
        }
        return null;
    }

    public static String toString(List<Long> values) {
        if (values != null && values.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.ensureCapacity(100);
            final int size = values.size();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(values.get(i));
            }
            if (size > 1) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
            return sb.toString();
        }
        return null;
    }

    public static String toString(ContentValues values) {
        if (values != null && values.keySet().size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.ensureCapacity(200);
            for (String key : values.keySet()) {
                sb.append(key).append('=').append(values.getAsString(key)).append(", ");
            }
            if (values.keySet().size() > 1) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
            return sb.toString();
        }
        return null;
    }

    public static String concat(String str1, String str2) {
        return String.format("%1$s%2$s", str1, str2);
    }

    public static String concat(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String buildCreateTableSql(String table, Column[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.ensureCapacity(150);
        sb.append("CREATE TABLE IF NOT EXISTS ").append(table).append('(');
        if (columns.length == 1) {
            sb.append(columns[0].buildCreateTableNeedSql());
        } else {
            for (int i = 0, length = columns.length; i < length; i++) {
                sb.append(columns[i].buildCreateTableNeedSql());
                if (i < (length - 1)) {
                    sb.append(',');
                }
            }
        }
        sb.append(");");
        return sb.toString();
    }

    public static void putCursorValue(ContentValues values, Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        int type = cursor.getType(columnIndex);
        switch (type) {
            case Cursor.FIELD_TYPE_INTEGER:
                values.put(columnName, cursor.getInt(columnIndex));
                break;
            case Cursor.FIELD_TYPE_FLOAT:
                values.put(columnName, cursor.getFloat(columnIndex));
                break;
            case Cursor.FIELD_TYPE_STRING:
                values.put(columnName, cursor.getString(columnIndex));
                break;
            case Cursor.FIELD_TYPE_BLOB:
                values.put(columnName, cursor.getBlob(columnIndex));
                break;
            case Cursor.FIELD_TYPE_NULL:
            default:
                values.putNull(columnName);
                break;
        }
    }

    public static boolean isFinishing(Activity activity) {
        return activity == null || activity.isFinishing()
                || (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1 && activity.isDestroyed());
    }

    public static Activity getActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
            if (context instanceof Activity) {
                return (Activity) context;
            }
        }
        return null;
    }

    public static boolean isFinishing(Context context) {
        return isFinishing(context, false);
    }

    public static boolean isFinishing(Context context, boolean defValue) {
        if (context instanceof Application) {
            return defValue;
        }
        Activity activity = getActivity(context);
        if (activity != null) {
            return isFinishing(activity);
        }
        return defValue;
    }

    public static boolean isFinishing(WeakReference<Activity> reference) {
        return reference != null && isFinishing(reference.get());
    }

    public static String[] toArray(long[] values) {
        final int size = values.length;
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = String.valueOf(values[i]);
        }
        return array;
    }

    public static String[] toArray(int[] values) {
        final int size = values.length;
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = String.valueOf(values[i]);
        }
        return array;
    }

    public static String[] toArray(List<?> values) {
        final int size = values.size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = values.get(i).toString();
        }
        return array;
    }
}
