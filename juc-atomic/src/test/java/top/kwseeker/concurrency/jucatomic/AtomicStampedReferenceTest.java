package top.kwseeker.concurrency.jucatomic;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 单纯的CAS并无法实现锁（需要volatile协助）
 */
public class AtomicStampedReferenceTest {

    @Test
    public void testAsr() throws InterruptedException {
        Holder holder1 = new Holder(0);
        Holder holder2 = new Holder(0);

        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                try {
                    latch.await();
                    holder1.num++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            latch.countDown();
        }
        Thread.sleep(100);
        System.out.println(holder1.num);

        AtomicStampedReference<Holder> holderRef = new AtomicStampedReference<>(holder2, 0);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                try {
                    latch.await();

                    Holder oldRef = holderRef.getReference();
                    int stamp = holderRef.getStamp();
                    boolean ret = holderRef.compareAndSet(oldRef, new Holder(oldRef.num + 1), stamp, stamp + 1);
                    if (!ret) {
                        System.out.println("cas failed");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            latch.countDown();
        }
        Thread.sleep(100);
        System.out.println(holderRef.getReference().num);
    }

    @Data
    @AllArgsConstructor
    static class Holder {
        private Integer num;
    }
}
