package com.kejunyao.db;

import android.text.TextUtils;

/**
 * 数据库表列
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public class Column {
    private static final String PRIMARY = "PRIMARY";
    private static final String KEY = "KEY";
    private static final String AUTOINCREMENT = "AUTOINCREMENT";

    private String name;
    private String type;
    private int size;
    private String constraint;

    public static Column create(String name) {
        return new Column(name);
    }

    public Column(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public Column name(String name) {
        this.name = name;
        return this;
    }

    public Column type(String type) {
        this.type = type;
        return this;
    }

    public Column size(int size) {
        this.size = size;
        return this;
    }

    public Column constraint(String constraint) {
        this.constraint = constraint;
        return this;
    }

    public Column textType() {
        this.type = "TEXT";
        return this;
    }

    public Column booleanType() {
        this.type = "BOOLEAN";
        return this;
    }

    public Column realType() {
        this.type = "REAL";
        return this;
    }

    public Column nvarcharType() {
        this.type = "NVARCHAR";
        return this;
    }

    public Column floatType() {
        this.type = "FLOAT";
        return this;
    }

    public Column doubleType() {
        this.type = "DOUBLE";
        return this;
    }

    public Column integerType() {
        this.type = "INTEGER";
        return this;
    }

    public Column longType() {
        this.type = "LONG";
        return this;
    }

    /**
     * 非空限制
     */
    public Column notNull() {
        this.constraint = " NOT NULL ";
        return this;
    }

    /**
     * 唯一约束
     */
    public Column unique() {
        this.constraint = " UNIQUE ";
        return this;
    }

    /**
     * 主键约束
     */
    public Column primaryKey() {
        this.constraint = " PRIMARY KEY ";
        return this;
    }

    /**
     * 主键约束自增
     */
    public Column primaryKeyAuto() {
        this.constraint = " PRIMARY KEY AUTOINCREMENT ";
        return this;
    }

    public Column foreignKey() {
        this.constraint = " FOREIGN KEY ";
        return this;
    }

    public Column check() {
        this.constraint = " CHECK ";
        return this;
    }

    public Column defaultValue(String value) {
        this.constraint = String.format(" DEFAULT('%s') ", value);
        return this;
    }

    public Column defaultValue(int value) {
        this.constraint = String.format(" DEFAULT('%d') ", value);
        return this;
    }

    public Column defaultNull() {
        this.constraint = " DEFAULT NULL ";
        return this;
    }

    public boolean isPrimaryKeyAuto() {
        if (constraint == null) {
            return false;
        }
        String uc = constraint.toUpperCase();
        int primaryIndex = uc.indexOf(PRIMARY);
        if (primaryIndex < 0) {
            return false;
        }
        int keyIndex = uc.indexOf(KEY);
        if (keyIndex < 0) {
            return false;
        }
        if (primaryIndex >= keyIndex) {
            return false;
        }
        int autoIndex = uc.indexOf(AUTOINCREMENT);
        return autoIndex > keyIndex;
    }

    public boolean isPrimaryKey() {
        if (constraint == null) {
            return false;
        }
        String uc = constraint.toUpperCase();
        if (uc.contains(AUTOINCREMENT)) {
            return false;
        }
        int primaryIndex = uc.indexOf(PRIMARY);
        if (primaryIndex < 0) {
            return false;
        }
        int keyIndex = uc.indexOf(KEY);
        if (keyIndex < 0) {
            return false;
        }
        return keyIndex > primaryIndex;
    }

    public String buildCreateTableNeedSql() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name).append(' ');
        if (!TextUtils.isEmpty(this.type)) {
            sb.append(' ').append(this.type);
            if (this.size > 0) {
                sb.append('(').append(this.size).append(')');
            }
            sb.append(' ');
        }
        if (!TextUtils.isEmpty(this.constraint)) {
            sb.append(' ').append(this.constraint).append(' ');
        }
        return sb.toString();
    }

    public String buildAddColumnSql(String table) {
        StringBuffer sb = new StringBuffer();
        sb.append("ALTER TABLE ").append(table)
                .append(" ADD ").append(this.name).append(' ');
        if (!TextUtils.isEmpty(this.type)) {
            sb.append(' ').append(this.type);
            if (this.size > 0) {
                sb.append('(').append(this.size).append(')');
            }
            sb.append(' ');
        }
        if (!TextUtils.isEmpty(this.constraint)) {
            sb.append(' ').append(this.constraint).append(' ');
        }
        sb.append(';');
        return sb.toString();
    }

}
