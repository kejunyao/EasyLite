package com.kejunyao.db;

/**
 * 数据库全局线程锁
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public interface Lock {
    Object GLOBAL_LOCK = Lock.class;
}
