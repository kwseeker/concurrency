package top.kwseeker.concurrency.jucatomic;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 从原子操作类中提取的主要的 Unsafe 操作方法
 * 1) 获取Unsafe类中静态变量unsafe
 * 2) 获取对象成员的偏移量
 */
public class UnsafeOperator {

    /**
     * 获取Unsafe单例实例
     */
    public static Unsafe getUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(Unsafe.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    /**
     * 获取成员的偏移量
     */
    public static long getFieldOffset(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return getUnsafe().objectFieldOffset(field);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
