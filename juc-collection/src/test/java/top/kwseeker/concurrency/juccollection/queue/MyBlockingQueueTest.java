package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Test;

import java.util.Random;

public class MyBlockingQueueTest {

    private MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(10);
    private Random random = new Random();

    @Test
    public void testMyBlockingQueue() {
        Thread[] producers = new Thread[10];
        Thread[] consumers = new Thread[10];
        for (int i = 0; i < 10; i++) {
            producers[i] = new Thread(()->{
                try {
                    int val = randomInt();
                    Thread.sleep(val);
                    System.out.println(Thread.currentThread().getName() + " to put ...");
                    queue.put(val);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        for (int i = 0; i < 10; i++) {
            consumers[i] = new Thread(()->{
                try {
                    int val = randomInt() + 1000;
                    Thread.sleep(val);
                    System.out.println(Thread.currentThread().getName() + " to take ...");
                    queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        for (Thread thread : consumers) {
            thread.start();
        }
        for (Thread thread : producers) {
            thread.start();
        }
        while (Thread.activeCount() > 2) {}
    }
    private int randomInt() {
        return random.nextInt(1000);
    }
}