package top.kwseeker.concurrency.thread;

import java.util.concurrent.*;

/**
 * DaemonThread 中的使用方式现实基本见不到，更常见的是使用线程池
 * 这里展示线程池中使用守护线程以及安全退出更加常见的方式（很多源码中使用这种方式）
 */
public class DaemonThreadInPool {

    public static void main(String[] args) throws InterruptedException {
        //普通线程任务线程池
        //这里设置核心线程数量为0，当任务执行完毕且超过30s，普通线程将不存在，之后守护线程会被强制关闭
        //设置核心线程数量>0，则此DEMO JVM 永远不会退出，因为总有普通线程一直在轮训查找任务队列是否有任务
        ExecutorService executor = new ThreadPoolExecutor(0, 2, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));
        //守护线程任务线程池
        ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setDaemon(true);
                        return thread;
                    }
                });

        scheduledExecutor.scheduleAtFixedRate(() -> {
            System.out.println("执行守护线程任务***");
            sleep(1000); //模拟任务执行耗时
            System.out.println("执行守护线程任务done");
        }, 0, 1, TimeUnit.SECONDS);

        for (int i = 0; i < 3; i++) {
            sleep(1000);
            executor.submit(() -> {
                System.out.println("执行普通线程任务...");
            });
        }

        //注册关闭钩子，shutdown() + awaitTermination() 不再接受新任务，但是会等待旧任务执行完毕，实现安全退出
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                //shutdown() + awaitTermination() 不再接受新任务，但是会等待旧任务执行完毕，实现安全退出
                //shutdown() 本质其实就是中断IDLE的工作者线程，
                //正在执行任务的线程会继续执行它们当前的任务，但是当它们执行完当前任务后，不会再从任务队列中获取新的任务来执行，而是会退出线程池
                scheduledExecutor.shutdown();   //TODO 源码还有很多细节
                System.out.println("awaitTerminate...");
                boolean b = scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS); //没有这个的话，会看到守护线程任务执行未完成JVM就退出了
                System.out.println("terminate result: " + b);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        sleep(100);
        System.out.println(">>>>>>> 主线程退出...");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
