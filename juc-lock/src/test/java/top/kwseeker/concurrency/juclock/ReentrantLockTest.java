package top.kwseeker.concurrency.juclock;

import org.junit.Test;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试锁：锁的多种获取方式，锁状态判断、锁释放、公平锁设置、多条件
 */
public class ReentrantLockTest {

    private int count = 0;

    @Test
    public void testReentrantLock() {
        ReentrantLock lock = new ReentrantLock();   //默认非公平
        //ReentrantLock lock = new ReentrantLock(true);
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                lock.lock();
                count++;
                lock.unlock();
            }, "thread-" + i).start();
        }

        while (Thread.activeCount() > 2) {
            Thread.yield();
        }
        System.out.println(count);
    }

    @Test
    public void testLockSupport() throws InterruptedException {
        //等待锁的线程
        Thread t = new Thread(() -> {
            LockSupport.park(this);
            System.out.println("waiting ...");
            boolean interrupted = Thread.interrupted();
            System.out.println("interrupted: " + interrupted);

        });
        t.start();

        //释放锁的线程
        Thread.sleep(10);
        System.out.println("state: " + t.getState());
        //等待线程退出的方式1：等待线程被唤醒
        //LockSupport.unpark(t);
        //等待线程退出的方式2：等待线程被中断
        t.interrupt();

        t.join();
        System.out.println("done");
    }
}
