package top.kwseeker.concurrency.concurrency_test;

import lombok.extern.slf4j.Slf4j;
import top.kwseeker.concurrency.annotation.NotThreadSafe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 使用Java 并发包中类 CountDownLatch 和 Semaphore 模拟并发
 *
 * CountDownLatch (java.util.concurrent.CountDownLatch) 使一个或多个线程等待一系列指定操作完成。
 * CountDownLatch以一个给定的数量初始化，
 * countDown() 每被调用一次，给定的数量减一，直到为0
 *
 * Semaphore 信号量 (java.util.concurrent.Semaphore) 是一个计数信号量
 * 计数信号量由一个指定数量N的"许可"初始化。
 * 每调用一次acquire(), 一个许可会被调用线程取走；
 * 每调用一次release(), 一个许可会被返还给信号量。
 * 在没有许可使用时，无法执行新的线程，从而保护一段代码最多只有N个线程进入。
 */
@Slf4j
//@NotThreadSafe
public class ConcurrencyTest {

    // 请求总数
    public static int clientTotal =5000;
    // 处理线程总数
    public static int threadTotal = 200;
    public static int count = 0;

    //synchronized private static void add() {
    private static void add() {
        count++;
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
    }
}
