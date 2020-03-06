package top.kwseeker.concurrency.juclock;

import org.junit.Test;

public class MyLockBySynchronizedTest {

    private int count = 0;

    @Test
    public void testMyLock() {
        int threadCount = 20;
        MyLockBySynchronized lock = new MyLockBySynchronized();
        Thread[] threads = new Thread[20];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 10000; j++) {
                    lock.lock();
                    count++;
                    lock.unlock();
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