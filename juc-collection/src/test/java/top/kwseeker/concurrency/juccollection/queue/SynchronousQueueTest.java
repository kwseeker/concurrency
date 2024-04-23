package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Test;

import java.util.concurrent.SynchronousQueue;

/**
 * 不保存元素
 * 前一个线程读/写会被阻塞，直到又有线程写/读
 */
public class SynchronousQueueTest {

    @Test
    public void testSynchronousQueue() throws InterruptedException {
        SynchronousQueue<String> queue = new SynchronousQueue<>();

        //先读
        Thread customer = new Thread(() -> {
            try {
                //poll() 超时时间0s,即立即返回
                System.out.println("customer non blocked get message from queue: " + queue.poll());
                //阻塞等待
                System.out.println("customer blocked get message from queue: " + queue.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        customer.start();

        Thread.sleep(2000);

        //后写
        Thread producer = new Thread(() -> {
            try {
                String message = "MessageA";
                System.out.println("producer put message to queue: " + message);
                queue.put(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        producer.start();

        customer.join();
        producer.join();
    }

    @Test
    public void testSynchronousQueue2() throws InterruptedException {
        SynchronousQueue<String> queue = new SynchronousQueue<>();

        //先写
        Thread producer = new Thread(() -> {
            try {
                String message = "MessageB";
                //offer() 超时时间0s,即立即返回
                boolean offer = queue.offer(message);
                System.out.println("producer non blocked offer message to queue: " + offer);
                //阻塞等待
                System.out.println("producer blocked put message from queue: " + message);
                queue.put(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        producer.start();

        Thread.sleep(2000);

        //后读
        Thread consumer = new Thread(() -> {
            try {
                String message = queue.take();
                System.out.println("consumer blocked take message from queue: " + message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        consumer.start();

        producer.join();
        consumer.join();
    }
}
