package top.kwseeker.concurrency.concurrent_module.singleton;

/**
 * 使用枚举实现的单例模式是线程安全的，利用了“所有的枚举值都是类静态常量（静态常量是在类加载之后初始化的），在初始化时会对所有的枚举值对象进行一次初始化”的原理
 *
 * 在枚举值中保存单例模式对象的实例，在枚举初始化方法中初始化单例对象实例。
 */
public class EnumSingleton {

    public static EnumSingleton getInstance() {
        return EnumInnerClass.INSTANCE.getInstance();
    }

    // some other fields and methods

    private enum EnumInnerClass {
        INSTANCE;

        private EnumSingleton instance;

        EnumInnerClass() {
            instance = new EnumSingleton();
        }

        public EnumSingleton getInstance() {
            return instance;
        }
    }

    public static void main(String[] args) {
        EnumSingleton instance = EnumSingleton.getInstance();
        System.out.println(instance.hashCode());
    }
}
