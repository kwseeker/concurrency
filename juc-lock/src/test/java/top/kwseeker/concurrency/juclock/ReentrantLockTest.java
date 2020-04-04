package top.kwseeker.concurrency.juclock;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试锁：锁的多种获取方式，锁状态判断、锁释放、公平锁设置、多条件
 */
public class ReentrantLockTest {

    private int count = 0;

    @Test
    public void testReentrantLock() {
        ReentrantLock lock = new ReentrantLock();   //默认非公平
        //ReentrantLock lock = new ReentrantLock(false);
        for (int i = 0; i < 3; i++) {
            new Thread(()->{
                lock.lock();
                count++;
                lock.unlock();
            }).start();
        }
        while (Thread.activeCount() > 2) {}
        System.out.println(count);
    }
}
