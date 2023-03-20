package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class DelayQueueTest {

    @Test
    public void testDelayQueue() throws Exception {
        DelayQueue<DelayedElement> queue = new DelayQueue<>();
        queue.offer(new DelayedElement("A", 1000));
        queue.offer(new DelayedElement("C", 3000));
        queue.offer(new DelayedElement("B", 2000));
        queue.offer(new DelayedElement("D", 4000));

        String[] elements = new String[]{"A", "B", "C", "D"};
        int i = 0;
        while (queue.size() > 0) {
            DelayedElement ele = queue.take();
            Assert.assertEquals(elements[i], ele.getElement());
            i++;
        }
    }

    @Test
    public void testMultiProducerAndConsumer() throws Exception {
        DelayQueue<DelayedElement> queue = new DelayQueue<>();
        queue.offer(new DelayedElement("A", 1000));
        queue.offer(new DelayedElement("C", 3000));
        queue.offer(new DelayedElement("B", 2000));
        queue.offer(new DelayedElement("D", 4000));

        System.out.println(System.currentTimeMillis());
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (;;) {
                    try {
                        DelayedElement ele = queue.take();
                        int costTime = ThreadLocalRandom.current().nextInt(2000) ;
                        System.out.println(Thread.currentThread().getName() + " handle element: " + ele.getElement() + " at " + System.currentTimeMillis() + " , cost time: " + costTime);
                        Thread.sleep(costTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        System.out.println("active thread count: " + Thread.activeCount());
        while (Thread.activeCount() > 2) {
            Thread.yield();
        }
        System.out.println("done");
    }

    @Test
    public void testTimeUnitConvert() {
        long ns = TimeUnit.NANOSECONDS.convert(5000, TimeUnit.MILLISECONDS);
        System.out.println(ns);
    }

    static class DelayedElement implements Delayed {
        private final String element;
        private final long deadline; //ms

        public DelayedElement(String element, long delayTime) {
            this.element = element;
            this.deadline = System.currentTimeMillis() + delayTime;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }

        public String getElement() {
            return element;
        }
    }
}
