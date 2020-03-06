package top.kwseeker.concurrency.threadsafety.synchronizer;

import org.junit.Test;

import java.util.Random;

public class BufferByLockConditionTest {

    private BufferByLockCondition buffer = new BufferByLockCondition();
    private Random random = new Random();
    @Test
    public void testBufferByLockCondition() {
        Thread[] producers = new Thread[5];
        Thread[] consumers = new Thread[5];
        for (int i = 0; i < 5; i++) {
            producers[i] = new Thread(()->{
                try {
                    int val = randomInt();
                    Thread.sleep(val);
                    System.out.println(Thread.currentThread().getName() + " to put ...");
                    buffer.put(randomInt());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        for (int i = 0; i < 5; i++) {
            consumers[i] = new Thread(()->{
                try {
                    int val = randomInt();
                    Thread.sleep(val);
                    System.out.println(Thread.currentThread().getName() + " to consume ...");
                    buffer.take();
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