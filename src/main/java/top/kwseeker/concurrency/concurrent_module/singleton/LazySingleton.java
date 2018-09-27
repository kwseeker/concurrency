package top.kwseeker.concurrency.concurrent_module.singleton;

/**
 * 线程安全的懒汉单例模式 (volatile + 双重检测 + 同步锁)
 */
public class LazySingleton {

    private volatile static LazySingleton lazySingleton = null; //使用volatile禁止第16行指令重排(严格按照 分配内存空间，初始化默认值，执行构造方法，连接引用与对象)

    private LazySingleton() {}

    public LazySingleton getInstance() {
        if(lazySingleton == null) {                             // volatile禁止指令重排可以防止有其他线程，执行到这里得到未完全初始化的变量
            synchronized (LazySingleton.class) {                // 添加同步锁防止其他线程重复构造对象。
                if(lazySingleton == null) {                     // 防止进入锁空间时有其他线程构造了对象。
                    lazySingleton = new LazySingleton();
                }
            }
        }
        return lazySingleton;
    }


}
