package top.kwseeker.concurrency.jucatomic;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 下面测试 AtomicLong 性能比 LongAdder 差 3倍多
 */
public class LongAdderTest {

    /**
     * 100000000, 407,462,956
     */
    @Test
    public void testLongAdder() throws Exception {
        LongAdder longAdder = new LongAdder();
        long start = System.nanoTime();
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < 50*1000*1000; i++) {
                longAdder.add(1L);
            }
        });
        Thread th2 = new Thread(() -> {
            for (int i = 0; i < 50*1000*1000; i++) {
                longAdder.add(1L);
            }
        });
        th1.start();
        th2.start();
        th1.join();
        th2.join();
        System.out.println(longAdder.sum() + ", " + (System.nanoTime() - start));
    }

    /**
     * 100000000, 1,523,314,436
     */
    @Test
    public void testAtomicLong() throws Exception {
        AtomicLong al = new AtomicLong(0);
        long start = System.nanoTime();
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < 50*1000*1000; i++) {
                al.incrementAndGet();
            }
        });
        Thread th2 = new Thread(() -> {
            for (int i = 0; i < 50*1000*1000; i++) {
                al.incrementAndGet();
            }
        });
        th1.start();
        th2.start();
        th1.join();
        th2.join();
        System.out.println(al.get() + ", " + (System.nanoTime() - start));
    }
}
