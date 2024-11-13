package top.kwseeker.concurrency;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

/**
 * 一个线程获取锁但是时间片用完会不会释放synchronized锁?
 * 已知线程 sleep() 不会、wait() 会；猜测 yield() 也不会，要是我们自己设计yield()也不会这么做，不然状态就太难控制了
 */
public class TimeSliceTest {

    @Test
    public void testSynchronizedTimeSlice() throws InterruptedException {
        Object lock = new Object();
        CountDownLatch latch = new CountDownLatch(2);

        Thread thread1 = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (lock) {
                System.out.println("thread1 获得锁");
                for (int i = 0; i < 10000; i++) {
                    Thread.yield();
                }
                System.out.println("thread1 将退出");
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (lock) {
                System.out.println("thread2 获得锁");
                for (int i = 0; i < 10000; i++) {
                    Thread.yield();
                }
                System.out.println("thread2 将退出");
            }
        });
        thread1.start();
        latch.countDown();
        thread2.start();
        latch.countDown();

        thread1.join();
        thread2.join();
    }
}
