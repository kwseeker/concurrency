package top.kwseeker.concurrency.sync;

/**
 * 修饰代码块, 借助 monitorenter monitorexit 竞争 ObjectMonitor
 */
public class SynchronizedCodeBlock {

    private int counter = 0;

    // public void incr();
    //    descriptor: ()V
    //    flags: ACC_PUBLIC
    //    Code:
    //      stack=3, locals=3, args_size=1
    //         0: aload_0
    //         1: dup
    //         2: astore_1
    //         3: monitorenter
    //         4: aload_0
    //         5: dup
    //         6: getfield      #2                  // Field counter:I
    //         9: iconst_1
    //        10: iadd
    //        11: putfield      #2                  // Field counter:I
    //        14: aload_1
    //        15: monitorexit
    //        16: goto          24
    //        19: astore_2
    //        20: aload_1
    //        21: monitorexit
    //        22: aload_2
    //        23: athrow
    //        24: return
    //      Exception table:
    //         from    to  target type
    //             4    16    19   any
    //            19    22    19   any
    //      LineNumberTable:
    //        line 11: 0
    //        line 12: 4
    //        line 13: 14
    //        line 14: 24
    //      LocalVariableTable:
    //        Start  Length  Slot  Name   Signature
    //            0      25     0  this   Ltop/kwseeker/concurrency/sync/SynchronizedCodeBlock;
    //      StackMapTable: number_of_entries = 2
    //        frame_type = 255 /* full_frame */
    //          offset_delta = 19
    //          locals = [ class top/kwseeker/concurrency/sync/SynchronizedCodeBlock, class java/lang/Object ]
    //          stack = [ class java/lang/Throwable ]
    //        frame_type = 250 /* chop */
    //          offset_delta = 4
    public void incr() {
        synchronized (this) {
            counter++;
        }
    }

    public int get() {
        return counter;
    }
}