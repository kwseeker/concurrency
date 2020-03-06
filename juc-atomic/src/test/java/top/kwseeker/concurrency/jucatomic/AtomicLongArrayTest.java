package top.kwseeker.concurrency.jucatomic;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * AtomicXxxArray测试，举例AtomicLongArray
 * 只能确保多线程同时读写数组中元素值互不影响时才是线程安全的；
 * 如果不同元素间值相互有影响则多线程下是不安全的。
 */
public class AtomicLongArrayTest {

    private static AtomicLongArray ala = new AtomicLongArray(2);

    /**
     * 10个线程同时修改数组中的两个成员
     */
    @Test
    public void testAtomicLongArray() {
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 10000; j++) {
                    //下面两个同时处理是线程安全的
                    //ala.incrementAndGet(0);
                    //ala.getAndIncrement(1);

                    //这样也是线程安全的
                    //ala.getAndIncrement(0);
                    //ala.getAndAdd(0, 1);

                    //改成这样是不安全的 结果：100000 5000110010（如果是线程安全则应该是5000050000）
                    ala.incrementAndGet(0);
                    ala.getAndAdd(1, ala.get(0));
                }
            });
        }
        for(Thread thread : threads) {
            thread.start();
        }
        while(Thread.activeCount() > 2) {}
        System.out.println(ala.get(0) + " " + ala.get(1));
    }
}
