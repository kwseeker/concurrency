package top.kwseeker.concurrency.jucatomic;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 通过 Unsafe 的 object 相关操作方法实现
 * 核心代码：
 *     private static final Unsafe unsafe = Unsafe.getUnsafe();
 *     private static final long valueOffset;
 *     valueOffset = unsafe.objectFieldOffset
 *                 (AtomicReference.class.getDeclaredField("value"));
 *     unsafe.putOrderedObject(this, valueOffset, newValue);
 *     unsafe.compareAndSwapObject(this, valueOffset, expect, update)
 *     unsafe.getAndSetObject(this, valueOffset, newValue);
 *
 */
public class AtomicReferenceTest {


    static class MultiCounter {

    }
}
