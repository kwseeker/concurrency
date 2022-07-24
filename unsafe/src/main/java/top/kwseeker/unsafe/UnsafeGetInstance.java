package top.kwseeker.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 获取Unsafe实例的两种方法
 * 1） 从getUnsafe方法的使用限制条件出发，通过Java命令行命令-Xbootclasspath/a把调用Unsafe相关方法的类A所在jar包路径追加到默认的bootstrap路径中，
 *  使得A被引导类加载器加载，从而通过Unsafe.getUnsafe方法安全的获取Unsafe实例。
 * 2）通过反射获取单例对象theUnsafe。
 */
public class UnsafeGetInstance {

    protected static Unsafe reflectGetUnsafe() {
        try {
            //Unsafe是单例模式对象，包含一个 theUnsafe 静态成员实例
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
