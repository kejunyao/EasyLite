package com.kejunyao.db;

import java.lang.reflect.Method;

/**
 * 注解工具类
 *
 * @author kejunyao
 * @since 2018年04月20日
 */
public final class AnnotationUtils {

    private AnnotationUtils() {
    }

    public static String getTransactionName(Class clazz, String transactionName, Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getName()).append('_');
        sb.append(Transaction.class.getSimpleName()).append('_');
        sb.append(transactionName);
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                sb.append('_').append(parameterTypes[i].getSimpleName());
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static boolean hasTransaction(Class clazz, String methodName, Class<?>... parameterTypes) {
        boolean has = false;
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            Transaction transaction = method.getAnnotation(Transaction.class);
            has = transaction != null;
        } catch (Exception e) {
        } finally {
            return has;
        }
    }

    @SuppressWarnings("unchecked")
    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
        } finally {
            return method;
        }
    }
}
