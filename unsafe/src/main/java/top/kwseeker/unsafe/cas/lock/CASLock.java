package top.kwseeker.unsafe.cas.lock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 使用CAS原子类实现锁
 */
public class CASLock {

    private final AtomicBoolean mutex = new AtomicBoolean(false);

    public void lock() {
        while (!mutex.compareAndSet(false, true)) {
            Thread.yield();
        }
    }

    public void unlock() {
        mutex.set(false);
    }
}
