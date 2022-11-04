package top.kwseeker.concurrency.thread.state;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程 sleep() 特点测试
 * 1) 会释放CPU资源，但是会保持拥有监视器锁
 *
 */
public class ThreadSleepTest {

    /**
     * 测试sleep()保持监视器锁
     * 结果：
     * thread1 start at 1667355544897
     * thread1 state: TIMED_WAITING
     * Got lock, at 1667355554897   （10s后，thread1退出后，主线程才获取到锁）
     */
    @Test
    public void testSleepHoldMonitorLock() throws InterruptedException {
        Object lock = new Object();
        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
        System.out.println("thread1 start at " + System.currentTimeMillis());
        Thread.sleep(100);  //等待thread1获取锁并进入 WAITING
        System.out.println("thread1 state: " + thread1.getState());
        synchronized (lock) {
            System.out.println("Got lock, at " + System.currentTimeMillis());
        }
    }

    /**
     * 对比 wait() 和 sleep(), wait() 不会保持监视锁
     * thread1 start at 1667355758651
     * thread1 state: TIMED_WAITING
     * Got lock, at 1667355758752 （主线程很快就获取到了锁）
     */
    @Test
    public void testWaitNotHoldMonitorLock() throws InterruptedException {
        Object lock = new Object();
        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
        System.out.println("thread1 start at " + System.currentTimeMillis());
        Thread.sleep(100);  //等待thread1获取锁并进入 TIMED_WAITING
        System.out.println("thread1 state: " + thread1.getState());
        synchronized (lock) {
            System.out.println("Got lock, at " + System.currentTimeMillis());
        }
    }

    @Test
    public void testSleepOnlyHoldMonitorLock() throws InterruptedException {
        //Object lock = new Object();
        ReentrantLock lock = new ReentrantLock();
        Thread thread1 = new Thread(() -> {
            lock.lock();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        thread1.start();
        System.out.println("thread1 start at " + System.currentTimeMillis());
        Thread.sleep(100);  //等待thread1获取锁并进入 TIMED_WAITING
        System.out.println("thread1 state: " + thread1.getState());

        try {
            lock.lock();
            System.out.println("main thread Got lock, at " + System.currentTimeMillis());
        } finally {
            lock.unlock();
        }
    }
}
