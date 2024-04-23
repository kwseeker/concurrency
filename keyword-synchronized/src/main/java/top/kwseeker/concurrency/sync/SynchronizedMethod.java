package top.kwseeker.concurrency.sync;

/**
 * 修饰方法, 借助 ACC_SYNCHRONIZED 竞争 ObjectMonitor
 */
public class SynchronizedMethod {

    private int counter = 0;

    //  public synchronized void incr();
    //    descriptor: ()V
    //    flags: ACC_PUBLIC, ACC_SYNCHRONIZED
    //    Code:
    //      stack=3, locals=1, args_size=1
    //         0: aload_0
    //         1: dup
    //         2: getfield      #2                  // Field counter:I
    //         5: iconst_1
    //         6: iadd
    //         7: putfield      #2                  // Field counter:I
    //        10: return
    //      LineNumberTable:
    //        line 11: 0
    //        line 12: 10
    //      LocalVariableTable:
    //        Start  Length  Slot  Name   Signature
    //            0      11     0  this   Ltop/kwseeker/concurrency/sync/SynchronizedMethod;
    public synchronized void incr() {
        counter++;
    }

    public int get() {
        return counter;
    }
}