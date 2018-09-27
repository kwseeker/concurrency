package top.kwseeker.concurrency.concurrent_module.collections.blockingQueues;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BlockingQueue(Interface)
 *
 *      SynchronousQueue            //==================================================================================
 *
 *      PriorityBlockingQueue       //==================================================================================
 *
 *      LinkedBlockingQueue         //==================================================================================
 *
 *      BlockingDeque(Interface)    //==================================================================================
 *          LinkedBlockingDeque
 *
 *      ArrayBlockingQueue          //==================================================================================
 *
 *      TransferQueue               //==================================================================================
 *          LinkedTransferQueue
 *
 *      DelayQueue<E extends Delayed>   //==============================================================================
 *          extends AbstractQueue<E>
 *          implements BlockingQueue<E>
 *
 *          延迟队列使用场景还挺多的，主要是做超时（延时）处理
 *              (如：Session超时处理、网络应答通信协议请求超时处理，缓存清除超时元素等）
 *
 *          延迟队列中的延迟数据，要等到延迟到期后才能够取出。
 *
 *          1）数据结构
 *          内部使用 PriorityQueue<E> 存储数据，使用ReentrantLock lock 控制同步，
 *          Condition available 控制阻塞（发送信号通知队列空或满状态），一个用于等待[队列头部]元素的线程Thread leader.
 *          PriorityQueue 是非线程安全的，DelayQueue是在其基础上添加了可重入锁进行并发同步;
 *          同时还要注意这个类有泛型下界约束，元素类型必须继承了Delayed接口。
 *
 *          PriorityQueue<E> extends AbstractQueue<E>
 *              implements java.io.Serializable
 *
 *          Delayed extends Comparable<Delayed>
 *
 *          2）公有方法
 *          和ArrayBlockingQueue基本一样
 *
 *          3）队列入列出列实现
 *          具体实现还是要先看 PriorityQueue<E> 的实现。
 */
//DelayQueue实现的一个缓存
@Slf4j
public class DelayQueueDemo {

    // 测试入口函数
    public static void main(String[] args) throws Exception {
        Cache<Integer, String> cache = new Cache<Integer, String>();
        cache.put(1, "aaaa", 3, TimeUnit.SECONDS);

        Thread.sleep(1000 * 2);
        {
            String str = cache.get(1);
            log.info(str);
        }

        Thread.sleep(1000 * 2);
        {
            String str = cache.get(1);
            log.info(str);
        }
    }

    //键值对对象
    static class Pair<K, V> {
        public K first;
        public V second;
        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    static class DelayItem<T> implements Delayed {
        private static final long NANO_ORIGIN = System.nanoTime();
        private static final AtomicLong sequencer = new AtomicLong(0);
        private final long sequenceNumber;
        private final long time;
        private final T item;

        final long now() {
            return System.nanoTime() - NANO_ORIGIN;
        }

        public DelayItem(T submit, long timeout) {
            this.time = now() + timeout;
            this.item = submit;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        public T getItem() {
            return this.item;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long d = unit.convert(time - now(), TimeUnit.NANOSECONDS);
            return d;
        }

        @Override
        public int compareTo(Delayed other) {
            if (other == this) // compare zero ONLY if same object
                return 0;
            if (other instanceof DelayItem) {
                DelayItem x = (DelayItem) other;
                long diff = time - x.time;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return 1;
                else if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }
            long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }
    }

    //使用DelayQueue实现的缓存
    static class Cache<K, V> {
        private ConcurrentMap<K, V> cacheObjMap = new ConcurrentHashMap<>();
        private DelayQueue<DelayItem<Pair<K, V>>> q = new DelayQueue<DelayItem<Pair<K, V>>>();

        private Thread daemonThread;

        public Cache() {
            Runnable daemonTask = new Runnable() {
                public void run() {
                    daemonCheck();
                }
            };

            daemonThread = new Thread(daemonTask);
            daemonThread.setDaemon(true);
            daemonThread.setName("Cache Daemon");
            daemonThread.start();
        }

        private void daemonCheck() {
            log.info("cache service started.");
            for (;;) {
                try {
                    log.info("DelayQueue size: {}, circle..", q.size());
                    DelayItem<Pair<K, V>> delayItem = q.take();
                    if (delayItem != null) {
                        // 超时对象处理
                        Pair<K, V> pair = delayItem.getItem();
                        cacheObjMap.remove(pair.first, pair.second); // compare and remove
                    }
                } catch (InterruptedException e) {
                    log.error("Exception: {}", e);
                    break;
                }
            }

            log.info("cache service stopped.");
        }

        // 添加缓存对象
        public void put(K key, V value, long time, TimeUnit unit) {
            V oldValue = cacheObjMap.put(key, value);
            if (oldValue != null)
                q.remove(key);

            long nanoTime = TimeUnit.NANOSECONDS.convert(time, unit);
            q.put(new DelayItem<Pair<K, V>>(new Pair<K, V>(key, value), nanoTime));     //DelayItem插入后nanoTime之后才能从队列中取出
        }

        public V get(K key) {
            return cacheObjMap.get(key);
        }
    }
}
