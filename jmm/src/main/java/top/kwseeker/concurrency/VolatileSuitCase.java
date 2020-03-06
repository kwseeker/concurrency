package top.kwseeker.concurrency;

/**
 * Volatile适合使用的场景:
 * 使用volatile实现并发控制，当volatile变量true值写入主内存后，所有线程可以立即停止。
 */
public class VolatileSuitCase {

    public static volatile boolean shutdownRequested;

    public static void shutdown() {
        shutdownRequested = true;
    }

    public static void doWork() {
        while(!shutdownRequested) {
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName() + " I have done!");
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                doWork();
            });
            thread.start();
        }
        Thread.sleep(1000);
        shutdown();
        Thread.sleep(100000);
    }
}
