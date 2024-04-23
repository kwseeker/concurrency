package top.kwseeker.concurrency.sync;

/**
 * 修饰静态方法, 借助 ACC_SYNCHRONIZED 竞争 ObjectMonitor
 */
public class SynchronizedStaticMethod {

    private static int counter = 0;

    // public static synchronized void incr();
    //    descriptor: ()V
    //    flags: ACC_PUBLIC, ACC_STATIC, ACC_SYNCHRONIZED
    //    Code:
    //      stack=2, locals=0, args_size=0
    //         0: getstatic     #2                  // Field counter:I
    //         3: iconst_1
    //         4: iadd
    //         5: putstatic     #2                  // Field counter:I
    //         8: return
    //      LineNumberTable:
    //        line 11: 0
    //        line 12: 8
    public synchronized static void incr() {
        counter++;
    }

    public int get() {
        return counter;
    }

    public synchronized int get2() {
        return counter;
    }
}