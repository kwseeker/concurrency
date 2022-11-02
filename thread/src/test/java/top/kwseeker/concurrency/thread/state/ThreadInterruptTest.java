package top.kwseeker.concurrency.thread.state;

import org.junit.Test;

/**
 * 线程中断做了什么：
 * 1）对于阻塞的线程，设置中断标志位true，待线程在检查中断标识时会抛出InterruptException并清空中断标志位：
 * 2）对于执行中的线程，仅仅设置中断标志位true。
 * 正确使用线程中断：
 * 1）对于等待状态的线程，抓取InterruptException异常，然后对应处理；
 * 2）对于执行中的线程，使用isInterrupted()和interrupted()检查中断标志位，然后对应处理。
 */
public class ThreadInterruptTest {

    private static int i = 0;

    @Test
    public void testInterrupt() throws InterruptedException {
        String cond = "lock".intern();
        Thread thread1 = new Thread(() -> {
            synchronized (cond) {
                try {
                    System.out.println("waiting");
                    cond.wait();
                } catch (InterruptedException | Error e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("out");
                }
            }
        });
        thread1.start();
        Thread.sleep(100);
        thread1.stop();
        synchronized (cond) {
            cond.notify();
        }
        thread1.join();
    }

    private void exec(long millis) {
        long begin = System.currentTimeMillis();
        while(System.currentTimeMillis() < begin + millis) {}
    }

    @Test
    public void testPrettyExit() throws InterruptedException {
        //这个线程可能 Runnable 和 Waiting 状态来回切换，所以既要设置标志位检查还要设置捕获InterruptException
        Thread thread = new Thread(()->{
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println(++i);
                    Thread.sleep(500);
                    System.out.println(++i);
                    exec(500);
                }
                System.out.println("thread exit");
            } catch (InterruptedException e) {
                System.out.println("catch interrupt signal, now exit");
                System.out.println(Thread.currentThread().isInterrupted()); //抛出异常后中断标志位被清空（false）。
            }
        });
        thread.start();
        Thread.sleep(3750);
        thread.interrupt();
        thread.join();
    }
}
