package top.kwseeker.concurrency.concurrent_module.atomic_case;

import lombok.extern.slf4j.Slf4j;
import top.kwseeker.concurrency.annotation.NotThreadSafe;
import top.kwseeker.concurrency.annotation.ThreadSafe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;


/**
 * 原子类的实现原理 CAS （Compare And Swap）
 * public final native boolean compareAndSwapObject(Object o, long offset, Object expected, Object x);
 * 循环通过 getObjectVolatile() 和 Object o、long offset 获取主内存中真实的值，然后与预期值（上次循环获取的值） expected 比较， 相等的时候才会更新为后面的新值 x 。
 *
 * 以AtomicInteger为例：
 *      public final int getAndAddInt(Object o, long offset, int delta) {
 *         int v;
 *         do {
 *             v = getIntVolatile(o, offset);                       //native方法
 *         } while (!compareAndSwapInt(o, offset, v, v + delta));   //native方法
 *         return v;
 *     }
 * 原理：
 *      两个线程A,B同时对值a+1操作，A线程获取a两次，值相同 准备把 v+1 写回主内存，这时B来取a, 获取预期值v, 然后再读取a一次并与v比较，发现值不同（说明之前有线程还没将数据写回）
 *      然后更新v, 再重新读a的值与v比较，发现相同，然后把 v+1 写回去。
 *
 * 疑问: TODO 有没有可能线程B读了两次，线程A都没完成写回主内存的操作？
 *
 */
@Slf4j
//@ThreadSafe
public class AtomicDetail {
    // 请求总数
    public static int clientTotal =5000;
    // 处理线程总数
    public static int threadTotal = 200;

    @NotThreadSafe
    public static int count = 0;
    // 线程安全处理
    @ThreadSafe
    public static AtomicInteger countAtomicInteger = new AtomicInteger(0);
    @ThreadSafe
    public static AtomicLong countAtomicLong = new AtomicLong(0);
    @ThreadSafe
    public static LongAdder countLongAdder = new LongAdder();

    private static void add() {
        count++;
        countAtomicInteger.incrementAndGet();
        countAtomicLong.incrementAndGet();
        countLongAdder.increment();
    }

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);

        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(()-> {
                try {
                    semaphore.acquire();    //保护部分开始
                    add();                  //同时执行 add() 的线程最多有200个
                    semaphore.release();    //保护部分结束
                } catch (InterruptedException e) {
                    log.error("Exception", e);
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();             //主线程等待线程池中线程计数完成
        } catch (InterruptedException e) {
            log.error("Exception", e);
        }
        executorService.shutdown();
        log.info("count:{}", count);
        log.info("count:{}", countAtomicInteger.get());
        log.info("count:{}", countAtomicLong.get());
        log.info("count:{}", countLongAdder);
    }

}
