package top.kwseeker.concurrency.jucexecutor.workprocess;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * 分析测试线程池关闭的源码
 */
public class ThreadPoolShutdownTest {

    @Test
    public void testShutdown() throws InterruptedException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(() -> {
            System.out.println(Thread.currentThread().getName() + " running");
        });

        es.shutdown();

        boolean terminated = es.awaitTermination(60, TimeUnit.SECONDS);
        if (terminated) {
            System.out.println("es terminated");
        }
    }

    //测试：线程池中没有工作者线程后不shutdown能否被回收
    //看源码中ThreadLocalPool有重写finalize()方法，方法中会执行shutdown()方法
    //-XX:+PrintGC -Xms10M -Xmx10M
    //sudo /usr/lib/jvm/default-java/bin/jmap -histo -F 28978 | grep ThreadPoolExecutor
    //测试结果：
    //只要线程池中没有工作者线程，线程池就可以被回收
    @Test
    public void testPoolNoThread() throws InterruptedException {
        ThreadPoolExecutor executor;
        for (int count = 0; count < 3000; count++) {
            System.out.println("count=" + count);
            executor = new ThreadPoolExecutor(1, 2, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
            executor.allowCoreThreadTimeOut(true);  //允许核心线程空闲时也可以回收
            //executor.allowCoreThreadTimeOut(true);  //允许核心线程空闲时也可以回收
            for (int i = 0; i < 2; i++) {
                final int fi = i;
                executor.submit(() -> {
                    System.out.println("task " + fi + " executed");
                });
            }

            if (count == 1000) {
                //等待这批线程池的任务执行完毕
                Thread.sleep(2000);
            }
        }

        System.out.println("Done!");
    }
}
