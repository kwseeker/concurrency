package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Test;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class LinkedTransferQueueTest {

    @Test
    public void testLinkedTransferQueue() throws InterruptedException {
        LinkedTransferQueue<String> queue = new LinkedTransferQueue<>();
        new Thread(() -> {
            try {
                String take = queue.take();
                System.out.println("T1: took:" + take);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "T1").start();
        new Thread(() -> {
            try {
                Thread.sleep(100);
                System.out.println("T2 start transfer");
                queue.transfer("Message A");
                queue.transfer("Message B");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "T2").start();
        new Thread(() -> {
            try {
                Thread.sleep(200);
                String take = queue.poll(1000, TimeUnit.MILLISECONDS);
                System.out.println("T3: took:" + take);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "T3").start();

        Thread.sleep(2000);
    }
}
