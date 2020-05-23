package com.kejunyao.log;

import android.os.SystemClock;

import java.util.WeakHashMap;

public final class TimeLogger {
    /**
     * log tag
     */
    private static final String TAG = "TimeLogger";
    /**
     * debug switch
     */
    private static final boolean DEBUG = Log.isLogEnabled();

    private static final WeakHashMap<String, TimeInfo> mTags = new WeakHashMap<>();

    /**
     *
     */
    private TimeLogger() {
    }

    public static void recordStart(String tag) {
        if (!DEBUG) {
            return;
        }
        synchronized (TimeLogger.class) {
            TimeInfo info = checkOrAddTag(tag);
            info.startTime = SystemClock.uptimeMillis();
            Log.dWithNoSwitch(TAG, "[", tag, "] start time: ", info.startTime);
        }
    }

    public static long recordEnd(String tag) {
        if (!DEBUG) {
            return -1;
        }
        synchronized (TimeLogger.class) {
            TimeInfo info = checkOrAddTag(tag);
            info.endTime = SystemClock.uptimeMillis();
            if (info.startTime < 0) {
                Log.i(TAG, "Tag must be in pair!");
                return -1;
            }
            long lastTime = info.getLastTime();
            Log.dWithNoSwitch(TAG, "[", tag, "] start time: ", info.startTime);
            Log.dWithNoSwitch(TAG, "[", tag, "] end time: ", info.endTime);
            Log.dWithNoSwitch(TAG, "[", tag, "] last: ", lastTime);
            mTags.remove(tag);
            return lastTime;
        }
    }

    /**
     * 从记录表中获取一条记录的时间信息，若不存在，则新建一条记录。
     *
     * @param tagName 记录的tag name。
     * @return 记录的时间信息。
     */
    private static TimeInfo checkOrAddTag(String tagName) {
        TimeInfo info = mTags.get(tagName);
        if (info == null) {
            info = new TimeInfo();
            info.tag = tagName;
            mTags.put(tagName, info);
        }
        return info;
    }

    static class TimeInfo {
        String tag;
        long startTime = -1;
        long endTime = -1;

        public long getLastTime() {
            return endTime - startTime;
        }
    }
}
