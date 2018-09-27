package top.kwseeker.concurrency.concurrent_module.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * 饿汉模式指在类被加载后就初始化实例，懒汉模式是在运行时实际用到时才会初始化
 *
 * 相比如果初始化过程很繁杂很耗时，则不适合饿汉模式；否则适合用饿汉模式。
 *
 * 饿汉模式是线程安全的
 *
 * 饿汉模式有两种实例初始化方式： new 和 static
 */
@Slf4j
public class HungerySingleton {

    //方法1
    //private static HungerySingleton hungerySingleton = new HungerySingleton();

    //方法2
    private static HungerySingleton hungerySingleton = null;    //引用必须位于静态块之前
    static {    //只会在类被加载时执行一次
        hungerySingleton = new HungerySingleton();
    }

    private HungerySingleton() {}

    public static HungerySingleton getInstance() {
        return hungerySingleton;
    }

    public static void main(String[] args) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.info("Exception", e);
        }
        System.out.println(getInstance().hashCode());
        System.out.println(getInstance().hashCode());
    }
}
