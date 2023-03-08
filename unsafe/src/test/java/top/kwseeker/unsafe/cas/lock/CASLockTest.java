package top.kwseeker.unsafe.cas.lock;

import org.junit.Test;

/**
 * 测试CAS实现的锁CASLock
 */
public class CASLockTest {

    private int count = 0;

    @Test
    public void testNoLock() {
        int threadCount = 20;
        Thread[] threads = new Thread[20];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 10000; j++) {
                    count++;
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        while (Thread.activeCount() > 2){}
        System.out.println("total:" + count);
    }

    @Test
    public void testCASLock() {
        CASLock casLock = new CASLock();
        int threadCount = 20;
        Thread[] threads = new Thread[20];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 10000; j++) {
                    casLock.lock();
                    count++;
                    casLock.unlock();
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        while (Thread.activeCount() > 2){}
        System.out.println("total:" + count);
    }
}