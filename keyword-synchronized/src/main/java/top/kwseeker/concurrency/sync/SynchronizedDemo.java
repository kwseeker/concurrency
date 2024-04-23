package top.kwseeker.concurrency.sync;

/**
 * 重新梳理下 synchronized 实现原理
 */
public class SynchronizedDemo {

    public static void main(String[] args) {
        SynchronizedMethod test1 = new SynchronizedMethod();
        SynchronizedStaticMethod test2 = new SynchronizedStaticMethod();
        SynchronizedCodeBlock test3 = new SynchronizedCodeBlock();
    }
}
