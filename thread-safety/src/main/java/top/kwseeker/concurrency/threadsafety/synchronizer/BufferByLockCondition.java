package top.kwseeker.concurrency.threadsafety.synchronizer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BufferByLockCondition {
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Object[] items = new Object[100];
    private int putptr, takeptr, count;

    public void put(Object x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length) { //Buffer满
                notFull.await();            //这里会释放锁，然后进入等待
            }
            System.out.println(Thread.currentThread().getName() + " produce value: " + x);
            items[putptr] = x;
            if(++putptr == items.length) {
                putptr = 0;
            }
            ++count;            //元素个数
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    //取值
    public Object take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await();   //等待写入
            }
            Object x = items[takeptr];
            System.out.println(Thread.currentThread().getName() + " consume value: " + x);
            if(++takeptr == items.length) {
                takeptr = 0;
            }
            --count;
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }
}
