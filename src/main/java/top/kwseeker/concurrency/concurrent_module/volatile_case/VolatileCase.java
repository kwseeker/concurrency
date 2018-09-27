package top.kwseeker.concurrency.concurrent_module.volatile_case;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * volatile 关键字的作用：
 * 1）保证其他线程对被修饰的变量的内存可见性（变量被更新，其他线程能够获取最新的值）；
 * 2）禁止乱序优化（指令重排序）。
 *
 * 应用实例
 * 1）用于标志变量（标志值改变后，可以立刻看到）
 *
 * 2）饿汉式双重校验锁的单例模式实现
 *      用于禁止重排序，初始化一个实例（SomeType st = new SomeType()）在java字节码中会有4个步骤，
 *      申请内存空间，初始化默认值（区别于构造器方法的初始化），执行构造器方法， 连接引用和实例；
 *      最后两个可能会重排序，如果先连接引用和实例，再执行构造器方法，从而有一段时间是未初始化完全的对象发布。
 *      这期间可能有另一个线程检查这个实例是否为空。
 */
@Slf4j
public class VolatileCase {
    private volatile boolean isOnRed = false;
    private volatile boolean isOnGreen = false;
    private volatile boolean isOnBlue = false;

    private AtomicInteger count = new AtomicInteger(1);

    //
    public void light() {
        if(isOnRed) {
            isOnGreen = false;
            isOnBlue = false;
            log.info("Red");
        } else if(isOnGreen) {
            isOnRed = false;
            isOnBlue = false;
            log.info("Green");
        } else if(isOnBlue) {
            isOnRed = false;
            isOnGreen = false;
            log.info("Blue");
        } else {
            isOnRed = false;
            isOnBlue = false;
            isOnGreen = false;
            log.info("Nul");
        }
    }


}
