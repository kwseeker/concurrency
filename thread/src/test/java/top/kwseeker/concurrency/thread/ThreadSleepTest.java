package top.kwseeker.concurrency.thread;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程 sleep() 和 Object wait() 特点测试
 * 1. 对于 sleep()方法，我们首先要知道该方法是属于 Thread 类中的。而 wait() 方法，则是属于 Object 类中的。
 * 2. sleep()方法导致了程序暂停执行指定的时间，让出 cpu 给其他线程，但是他的监控状态依然保持着，当指定的时间到了又会自动恢复运行状态。
 * 3. wait() notify() 依赖 synchronized 监视器锁才能正常工作。
 * 4. 在调用 sleep()方法的过程中，线程不会释放监视器锁。
 * 5. 当调用 wait()方法的时候，线程会放弃监视器锁，进入等待此对象的等待锁定池，只有针对此对象调用 notify()方法后本线程才进入对象锁定池准备获取对象锁进入运行状态。
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

    /**
     * sleep() 也会保持 ReentrantLock这种锁，从实现原理上看是显而易见的(当时为何会做这种测试？)
     * wait() 肯定也会保持 ReentrantLock这种锁
     */
    @Test
    public void testSleepAlsoHoldAQSLock() throws InterruptedException {
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
