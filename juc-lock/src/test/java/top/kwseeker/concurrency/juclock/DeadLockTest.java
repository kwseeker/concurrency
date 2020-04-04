package top.kwseeker.concurrency.juclock;

import org.junit.Test;

public class DeadLockTest {

    private Object monitor1 = new Object();
    private Object monitor2 = new Object();

    @Test
    public void testDeadLock() throws InterruptedException {
        Thread thread1 = new Thread(()->{
            synchronized (monitor1) {
                System.out.println("获取monitor1，请求monitor2 ...");
                synchronized (monitor2) {
                    System.out.println("再次获取monitor2");
                }
            }
        });
        Thread thread2 = new Thread(()->{
            synchronized (monitor2) {
                System.out.println("获取monitor2，请求monitor1 ...");
                synchronized (monitor1) {
                    System.out.println("再次获取monitor1 ...");
                }
            }
        });
        thread1.start();
        //Thread.sleep(100);
        thread2.start();
        while (Thread.activeCount() > 2) {}
    }
}
