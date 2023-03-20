package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * PriorityBlockingQueue 源码测试
 */
public class PriorityBlockingQueueTest {

    @Test
    public void testOfferAndPoll() throws InterruptedException {
        PriorityBlockingQueue<String> queue = new PriorityBlockingQueue<>(3);
        //入队
        //PriorityBlockingQueue入队其实都是offer(E e)实现的
        queue.put("A");     //等同于offer("A)
        queue.add("B");        //等同于offer("B")
        queue.offer("C");
        queue.offer("D", 5, TimeUnit.SECONDS);  //等同于offer("D"), PriorityBlockingQueue 入队操作根本不会阻塞
        queue.offer("E");
        //出队：
        String element = queue.poll();
        Assert.assertEquals("A", element);

        //先清空
        queue.clear();
        Thread thread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                queue.offer("F");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        //将阻塞，子线程５s后插入元素后继续执行
        //element = queue.take();
        element = queue.poll(10, TimeUnit.SECONDS);
        Assert.assertEquals("F", element);
    }
}
