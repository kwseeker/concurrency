package top.kwseeker.concurrency.concurrent_module.AQSClass;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 类似于CountDownLatch,不过它是从0开始直到达到某个值，await()的线程才会继续
 * CyclicBarrier的计数值是可以重置的,可以指定满足条件后的回调函数
 *
 * 也是用于有依赖关系的线程控制，这个思想是批量处理，积累了一堆任务，最后一起继续执行
 *
 * 应用场景：
 *
 * TODO: 感觉这个适合做算法性能测试
 */
@Slf4j
public class CyclicBarrierDemo {

//    private static CyclicBarrier barrier = new CyclicBarrier(5);    ////////积累够5个线程，最后一起继续执行
    private static CyclicBarrier barrier = new CyclicBarrier(5, ()->{   ////////带回调函数的CyclicBarrier
        log.info("callback is running");
    });

    public static void main(String[] args) throws Exception {

        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            Thread.sleep(1000);
            executor.execute(() -> {
                try {
                    race(threadNum);
                } catch (Exception e) {
                    log.error("exception", e);
                }
            });

            if(i == 3) {    //当i==3时，
                log.info("等待线程数量：{}， Parties:{}", barrier.getNumberWaiting(), barrier.getParties());    //the number of parties required to trip this barrier
                barrier.reset();    ////////重置barrier，唤醒所有的等待线程
            }
        }
        executor.shutdown();
    }

    private static void race(int threadNum) throws Exception {
        Thread.sleep(1000);
        log.info("{} is ready", threadNum);
//        barrier.await();
        barrier.await(20, TimeUnit.SECONDS); //不管CyclicBarrier等待的线程个数是否达到5，只要超过20S钟，此线程就超时，而此线程超时其他线程还在等待，其他线程就会抛出异常
        //BrokenBarrierException if <em>another</em> thread was
        //     *         interrupted or timed out while the current thread was
        //     *         waiting, or the barrier was reset, or the barrier was broken
        //     *         when {@code await} was called, or the barrier action (if
        //     *         present) failed due to an exception
        log.info("{} continue", threadNum);
    }
}
