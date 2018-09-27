package top.kwseeker.concurrency.concurrent_module.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AQS(AbstractQueuedSynchronizer) 数据结构中有两个链表，一个Sync queue 一个 Condition queue
 * ReentrantLock 是在 AQS基础上实现的锁
 *
 * ReentrantLock也包含Condition ， 可以实现线程的休眠和唤醒。
 *
 * 应用实例：
 *      可用于实现一个简单的有界队列（或者缓冲），队列为空时，队列的删除操作将会阻塞直到队列中有新的元素，
 *      队列已满时，队列的插入操作将会阻塞直到队列出现空位。
 *
 */
@Slf4j
public class AqsLockConditionDemo {

    public static void main(String[] args) {
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();

        new Thread(() -> {
            try {
                reentrantLock.lock();
                log.info("wait signal"); // 1
                condition.await();              //释放锁，然后将线程加入Condition等待队列，等待被唤醒
                                                //一旦被唤醒,且锁未被其他任意线程占用，则此线程会被重新加锁
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("get signal"); // 4
            reentrantLock.unlock();
        }).start();

        new Thread(() -> {
            reentrantLock.lock();
            log.info("get lock"); // 2
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            condition.signalAll();              //唤醒Condition等待队列中的所有线程
            log.info("send signal ~ "); // 3
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reentrantLock.unlock();
        }).start();
    }
}
